package com.binzc.sandbox.executor.impl;

import com.binzc.sandbox.executor.CodeExecutor;
import com.binzc.sandbox.model.*;
import com.binzc.sandbox.service.Impl.DockerImageService;
import com.binzc.sandbox.utils.FileUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class JavaCodeExecutor implements CodeExecutor {

    @Autowired
    private DockerImageService dockerImageService;

    @Autowired
    private DockerClient dockerClient;

    @Value("${sandbox.mount-path}")
    private String mountPath;

    private static final long TIME_OUT = 25000L;
    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) {
        String code = request.getCode();
        String language = request.getLanguage();
        List<String> inputList=request.getInputList();
        // 保存文件，放回文件绝对路径
        String absolute= FileUtils.saveFile(code,language);
        //下载镜像
        try{
            dockerImageService.ensureImageExists(ImageEnum.JavaImage);
        }catch (Exception e){
            throw new RuntimeException("下载镜像失败");
        }
        //创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(ImageEnum.JavaImage.getImageName());
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(512 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        //        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        String phsicalPath =FileUtils.MapToPhysicalPath(mountPath, absolute);
        hostConfig.setBinds(new Bind(phsicalPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withWorkingDir("/app")  // 设置工作目录
                .withCmd("tail", "-f", "/dev/null")
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();
        log.info("创建容器："+createContainerResponse.toString());
        String containerId = createContainerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        //编译阶段
        {
            // 创建命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("javac", "Main.java")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();
            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
            ExecStartResultCallback callback = new ExecStartResultCallback(stdoutStream, stderrStream);
            // 直接开始执行命令
            try {
                dockerClient.execStartCmd(execId)
                        .withDetach(false)
                        .withTty(false)
                        .exec(callback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException("JAVA编译失败");
            }
            //获得码
            InspectExecResponse execResponse = dockerClient.inspectExecCmd(execId).exec();
            Integer exitCode = execResponse.getExitCode();
            String errorMessage=null;
            try {
                String message = stdoutStream.toString("UTF-8");
                errorMessage = stderrStream.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("字符集不匹配");
            }
            if(exitCode==null||exitCode!=0){
                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
                executeCodeResponse.setStatus(exitCode);
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setExecuteMessageList(null);
                return executeCodeResponse;
            }

        }


        List<ExecuteMessage>data=new ArrayList<>();
        for(String inputArgs:inputList){
            // 处理输入
            String inputHandled = String.join("\n", inputArgs.split("\\s+"))+"\n";

            // 创建命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("java","Main")
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();

            // 输入，输出，错误流
            InputStream inputStream = new ByteArrayInputStream(inputHandled.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
            ExecStartResultCallback callback = new ExecStartResultCallback(stdoutStream, stderrStream);

            //提前开始监控内存
            final long[] maxMemory = {0L};
            // 获取占用的内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);

            long costTime=0;
            try {
                //开始监控时间
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                // 直接开始执行命令
                dockerClient.execStartCmd(execId)
                        .withStdIn(inputStream)
                        .withDetach(false)
                        .withTty(false)
                        .exec(callback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);

                //计时结束
                stopWatch.stop();
                costTime = stopWatch.getTotalTimeMillis();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("等待 stats 回调失败", e);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                statsCmd.close();
            }
            //获得码
            InspectExecResponse execResponse = dockerClient.inspectExecCmd(execId).exec();
            Integer exitCode = execResponse.getExitCode();

            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(exitCode);
            executeMessage.setTime(costTime);
            executeMessage.setMemory(maxMemory[0]);
            try {
                String message = stdoutStream.toString("UTF-8");
                String errorMessage = stderrStream.toString("UTF-8");
                executeMessage.setMessage(message);
                executeMessage.setErrorMessage(errorMessage);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("字符集不匹配");
            }
            data.add(executeMessage);
            try {
                inputStream.close();
                stdoutStream.close();
                stderrStream.close();
            } catch (IOException e) {
                throw new RuntimeException("关不掉了");
            }

        }
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        FileUtils.deleteFile(absolute);
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        executeCodeResponse.setExecuteMessageList(data);
        executeCodeResponse.setStatus(0);
        executeCodeResponse.setMessage("代码沙箱执行无误");
        return executeCodeResponse;
    }

    @Override
    public LanguageEnum language() {
        return LanguageEnum.JAVA;
    }
}

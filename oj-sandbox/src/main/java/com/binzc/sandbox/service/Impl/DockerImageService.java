package com.binzc.sandbox.service.Impl;

import com.binzc.sandbox.model.ImageEnum;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DockerImageService {
    @Resource
    private DockerClient dockerClient;
    public DockerImageService(DockerClient dockerClient){
        this.dockerClient= dockerClient;
    }

    /**
     * 确保镜像存在，如果不存在则拉取
     * @param imageEnum 镜像名称，例如 "python:3.9"
     * @throws Exception 拉取失败时抛出异常
     */
    public void ensureImageExists(ImageEnum imageEnum) throws Exception {
        String imageName = imageEnum.getImageName();
        if (!isImagePresent(imageEnum)) {
            pullImage(imageEnum);
        }
    }

    /**
     * 检查本地是否存在指定镜像
     */
    private boolean isImagePresent(ImageEnum imageEnum) {
        String imageName = imageEnum.getImageName();
        try {
            InspectImageResponse image = dockerClient.inspectImageCmd(imageName).exec();
            return image != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * 拉取指定镜像
     */
    private void pullImage(ImageEnum imageEnum) throws Exception {
        String imageName = imageEnum.getImageName();
        log.info("开始拉取镜像: " + imageName);

        dockerClient.pullImageCmd(imageName)
                .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>())
                .awaitCompletion(20, TimeUnit.MINUTES); // 设置超时时间为10分钟

        log.info("镜像拉取完成: " + imageName);
    }


}

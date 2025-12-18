package com.binzc.sandbox.controller;

import com.binzc.sandbox.common.BaseResponse;
import com.binzc.sandbox.common.ResultUtils;
import com.binzc.sandbox.model.ExecuteCodeRequest;
import com.binzc.sandbox.model.ExecuteCodeResponse;
import com.binzc.sandbox.service.SandBoxService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CodeController {
    @Autowired
    private DockerClient dockerClient;

    @Resource
    private SandBoxService sandBoxService;

    /**
     * @Description: 查看docker版本
     * @return
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        Version exec = dockerClient.versionCmd().exec();
        return ResultUtils.success(exec.toString());
    }
    @PostMapping("/executeCode")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = sandBoxService.executeCode(executeCodeRequest);
        return ResultUtils.success(executeCodeResponse);
    }



}

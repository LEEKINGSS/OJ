package com.binzc.sandbox.service;

import com.binzc.sandbox.model.ExecuteCodeRequest;
import com.binzc.sandbox.model.ExecuteCodeResponse;

/**
 * 沙箱服务接口
 */
public interface SandBoxService {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}

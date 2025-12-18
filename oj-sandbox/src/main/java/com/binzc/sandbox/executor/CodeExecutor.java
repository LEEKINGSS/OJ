package com.binzc.sandbox.executor;

import com.binzc.sandbox.model.ExecuteCodeRequest;
import com.binzc.sandbox.model.ExecuteCodeResponse;
import com.binzc.sandbox.model.LanguageEnum;

public interface CodeExecutor {
    /**
     * 执行代码
     * @param request
     * @return
     */
    ExecuteCodeResponse execute(ExecuteCodeRequest request);

    /**
     * 支持的语言
     * @return
     */

    LanguageEnum language();
}

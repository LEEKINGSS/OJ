package com.binzc.oj.judge.codesandbox.impl;

import com.binzc.oj.judge.codesandbox.CodeSandBox;
import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("ThirdPartyCodeSandBox executeCode");
        return null;
    }

    @Override
    public CodeSandBoxType getCodeSandBoxType() {
        return CodeSandBoxType.THIRDPARTY;
    }
}

package com.binzc.oj.judge.codesandbox;

import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeResponse;

public class CodeSandBoxProxy implements CodeSandBox{
    private CodeSandBox codeSandBox;

    public CodeSandBoxProxy(CodeSandBox codeSandBox) {
        this.codeSandBox = codeSandBox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("代理前，输出日志");
        ExecuteCodeResponse executeCodeResponse=codeSandBox.executeCode(executeCodeRequest);
        System.out.println("代理后，输出日志");
        return executeCodeResponse;
    }

    @Override
    public CodeSandBoxType getCodeSandBoxType() {
        return null;
    }
}

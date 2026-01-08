package com.binzc.oj.judge.codesandbox.impl;

import com.binzc.oj.judge.codesandbox.CodeSandBox;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.binzc.oj.judge.codesandbox.model.ExecuteMessage;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.enums.JudgeInfoMessageEnum;
import com.binzc.oj.model.enums.QuestionSubmitStatusEnum;
import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 创建示例执行消息
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        ExecuteMessage executeMessage = new ExecuteMessage();
        executeMessage.setMessage("Hello World\n示例输出：代码执行成功");
        executeMessage.setErrorMessage(null);
        executeMessage.setTime(100L);
        executeMessage.setMemory(10240L); // 10KB
        executeMessage.setExitValue(0);
        executeMessageList.add(executeMessage);
        
        // 构建响应
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setExecuteMessageList(executeMessageList);
        executeCodeResponse.setMessage("示例沙箱执行成功");
        executeCodeResponse.setStatus(0);
        
        return executeCodeResponse;
    }

    @Override
    public CodeSandBoxType getCodeSandBoxType() {
        return CodeSandBoxType.EXAMPLE;
    }
}

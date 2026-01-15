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
//        List<String> inputList = executeCodeRequest.getInputList();
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.setOutputList(inputList);
//        executeCodeResponse.setMessage("测试执行成功");
//        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
//        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
//        judgeInfo.setMemory(100L);
//        judgeInfo.setTime(100L);
//        executeCodeResponse.setJudgeInfo(judgeInfo);
        return null;
    }

    @Override
    public CodeSandBoxType getCodeSandBoxType() {
        return CodeSandBoxType.EXAMPLE;
    }
}

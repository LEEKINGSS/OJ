package com.binzc.oj.judge.strategy;

import com.binzc.oj.judge.codesandbox.model.ExecuteMessage;
import com.binzc.oj.judge.codesandbox.model.JudgeMessage;
import com.binzc.oj.judge.codesandbox.model.JudgeResult;
import com.binzc.oj.model.dto.question.JudgeConfig;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.enums.JudgeInfoMessageEnum;

import java.util.ArrayList;
import java.util.List;

public class PythonLanguageJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeResult doJudge(JudgeContext judgeContext) {
        // 准备工作
        List<ExecuteMessage> executeMessages = judgeContext.getExecuteMessages();
        List<String>outputList=judgeContext.getOutputList();
        JudgeConfig judgeConfig= judgeContext.getJudgeConfig();
        List<JudgeMessage>judgeMessages=new ArrayList<>();
        JudgeInfo judgeInfo = new JudgeInfo();
        if(executeMessages.size()!=outputList.size()){
            throw new RuntimeException("逆天错误，不知道为什么长度对不上");
        }

        int n=executeMessages.size();
        Long maxTime=-1L,maxMemory=-1L;
        boolean success=true;
        for(int i=0;i<n;i++){
            ExecuteMessage executeMessage=executeMessages.get(i);
            int exitValue=executeMessage.getExitValue();
            Long time=executeMessage.getTime();
            Long memory=executeMessage.getMemory()/100000;
            JudgeMessage judgeMessage=new JudgeMessage();
            judgeMessage.setTime(time);
            judgeMessage.setMemory(memory);
            // 程序出错
            if(exitValue!=0){
                success=false;
                judgeMessage.setStatus(JudgeInfoMessageEnum.RUNTIME_ERROR.getText());
                judgeMessage.setStatusSingle(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue()+
                        "\n"+"错误码："+exitValue
                        +"\n"+"错误信息："+executeMessage.getErrorMessage()
                );
            }
            // 超时，超内存，答案错误|正确
            else {
                if (time != null) {
                    maxTime = Math.max(time, maxTime);
                }
                if(memory!=null){
                    maxMemory=Math.max(memory,maxMemory);
                }
                //超时
                if(time>judgeConfig.getTimeLimit()*10){
                    success=false;
                    judgeMessage.setStatus(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getText());
                    judgeMessage.setStatusSingle(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
                } else if (memory > judgeConfig.getMemoryLimit()*10) {
                    success=false;
                    judgeMessage.setStatus(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getText());
                    judgeMessage.setStatusSingle(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
                }else{
                    String expectedAnswer=outputList.get(i);
                    String actualAnswer=executeMessage.getMessage();
                    String[] arr1 = expectedAnswer.trim().split("\\s+");
                    String[] arr2 = actualAnswer.trim().split("\\s+");

                    boolean same=true;
                    String appendMessage=null;

                    // 获取最短数组长度，防止越界
                    int len = Math.min(arr1.length, arr2.length);

                    for (int j = 0; j < len; j++) {
                        if (!arr1[j].equals(arr2[j])) {
                            same=false;
                            appendMessage="Expected: "+arr1[j]+" Actual: "+arr2[j];
                            break;
                        }
                    }
                    if(same){
                        success=true;
                        judgeMessage.setStatus(JudgeInfoMessageEnum.ACCEPTED.getText());
                        judgeMessage.setStatusSingle(JudgeInfoMessageEnum.ACCEPTED.getValue());
                    }else{
                        success=false;
                        judgeMessage.setStatus(JudgeInfoMessageEnum.WRONG_ANSWER.getText());
                        judgeMessage.setStatusSingle(JudgeInfoMessageEnum.WRONG_ANSWER.getValue()+"\n"+appendMessage);
                    }
                }

            }
            judgeMessages.add(judgeMessage);
        }
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime(maxTime);
        judgeInfo.setMessage(success?"Accepted":"Unaccepted");
        JudgeResult judgeResult = new JudgeResult();
        judgeResult.setJudgeInfo(judgeInfo);
        judgeResult.setJudgeMessages(judgeMessages);
        return judgeResult;
    }
}

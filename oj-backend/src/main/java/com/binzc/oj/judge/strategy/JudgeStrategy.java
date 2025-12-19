package com.binzc.oj.judge.strategy;

import com.binzc.oj.judge.codesandbox.model.JudgeResult;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;

public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeResult doJudge(JudgeContext judgeContext);

}

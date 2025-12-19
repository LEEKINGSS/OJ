package com.binzc.oj.judge;

import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.QuestionSubmit;

public interface JudgeService {
    /**
     * 判题服务
     * @param questioSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questioSubmitId);
}

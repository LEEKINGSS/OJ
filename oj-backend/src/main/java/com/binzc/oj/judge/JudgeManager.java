package com.binzc.oj.judge;


import com.binzc.oj.judge.codesandbox.model.JudgeResult;
import com.binzc.oj.judge.strategy.*;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeResult doJudge(JudgeContext judgeContext) {
        String language = judgeContext.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equalsIgnoreCase(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        } else if ("cpp".equalsIgnoreCase(language)) {
            judgeStrategy=new CppLanguageJudgeStrategy();
        } else if ("python".equalsIgnoreCase(language)) {
            judgeStrategy=new PythonLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
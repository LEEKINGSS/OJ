package com.binzc.oj.service;

import com.binzc.oj.model.dto.questionsubmit.QueryParmRequest;
import com.binzc.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.model.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author binzc
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2025-05-08 21:02:49
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 获取用户最新提交代码
     * @param questionId
     *
     * @param userId
     * @return
     */
    CodeVo getCode(long questionId, long userId);

    /**
     *
     * @param questionSubmitAddRequest
     * @param user
     * @return
     */
    QuestionSubmitVo doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User user);

    /**
     * 获取提交记录
     * @param queryParmRequest
     * @return
     */
    SubmitRecodWithPageVo getSubmitRecords(QueryParmRequest queryParmRequest);
    /**
     * 获取提交记录详情
     * @param submitId
     * @return
     */
    SubmitRecordDetail getSubmitRecordDetail(long submitId);


}

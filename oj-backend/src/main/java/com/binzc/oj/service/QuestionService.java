package com.binzc.oj.service;

import com.binzc.oj.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.binzc.oj.model.vo.QuestionDetailSafeVo;
import com.binzc.oj.model.vo.QuestionListSafeVo;

import java.util.LinkedList;
import java.util.List;

/**
* @author binzc
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-05-08 21:02:49
*/
public interface QuestionService extends IService<Question> {
    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取题目列表
     * @param tags
     * @param name
     * @param page
     * @param size
     */
    QuestionListSafeVo getQuestionList(List<String> tags, String name, int page, int size);

    /**
     * 根据题目id获取题目
     * @param id
     * @return
     */
    QuestionDetailSafeVo getQuestionById(long id);
}

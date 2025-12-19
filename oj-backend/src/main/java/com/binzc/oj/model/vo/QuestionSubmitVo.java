package com.binzc.oj.model.vo;

import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.binzc.oj.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交封装类
 * @TableName question
 */
@Data
public class QuestionSubmitVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 提交用户信息
     */
    private LoginUserVO loginUserVO;

    /**
     * 对应题目信息
     */
    private QuestionDetailSafeVo questionDetailSafeVo;

    /**
     * 包装类转对象
     *
     * @param questionSubmitVO
     * @return
     */
    private static final long serialVersionUID = 1L;

    public static QuestionSubmitVo generateVo(QuestionSubmit questionSubmit, Question question, User user){
        QuestionSubmitVo questionSubmitVo = new QuestionSubmitVo();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVo);
        questionSubmitVo.setQuestionDetailSafeVo(QuestionDetailSafeVo.generateVo(question));
        questionSubmitVo.setLoginUserVO(LoginUserVO.generateVo(user));
        return questionSubmitVo;
    }
}
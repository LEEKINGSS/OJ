package com.binzc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.binzc.oj.judge.codesandbox.model.ExecuteMessage;
import com.binzc.oj.judge.codesandbox.model.JudgeMessage;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.model.enums.QuestionSubmitStatusEnum;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SubmitRecordDetail {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     *  判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 每个测试点信息
     */
    private List<JudgeMessage> judgeMessages;

    /**
     * 判题状态
     */
    private Integer status;

    /**
     * 提交状态信息
     */

    private String message;

    /**
     * 提交用户id
     */
    private Long userId;
    /**
     * 用户名字
     */
    private String userName;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 题目id
     */
    private Long questionId;
    /**
     * 题目名字
     */
    private String questionTitle;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     * @param user
     * @param questionSubmit
     * @param question
     * @return
     */

    public static SubmitRecordDetail generateVo(User user, QuestionSubmit questionSubmit, Question question) {
        SubmitRecordDetail submitRecordDetail = new SubmitRecordDetail();
        submitRecordDetail.setUserId(user.getId());
        submitRecordDetail.setUserName(user.getUserName());
        submitRecordDetail.setQuestionId(question.getId());
        submitRecordDetail.setQuestionTitle(question.getTitle());
        submitRecordDetail.setId(questionSubmit.getId());
        submitRecordDetail.setLanguage(questionSubmit.getLanguage());
        submitRecordDetail.setCode(questionSubmit.getCode());
        submitRecordDetail.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(),JudgeInfo.class));
        submitRecordDetail.setJudgeMessages(JSONUtil.toList(questionSubmit.getJudgeMessages(), JudgeMessage.class));
        Integer status = questionSubmit.getStatus();
        submitRecordDetail.setStatus(status);
        submitRecordDetail.setMessage(QuestionSubmitStatusEnum.getEnumByValue(status).getText());
        submitRecordDetail.setCreateTime(questionSubmit.getCreateTime());
        submitRecordDetail.setUserAvatar(user.getUserAvatar());
        return submitRecordDetail;
    }

}

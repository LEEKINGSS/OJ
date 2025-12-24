package com.binzc.oj.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: binzc
 * @Description: 用户展示提交记录列表的信息
 */

@Data
public class SubmitRecordSimple {
    /**
     * 提交记录id
     */
    private Long submitId;

    //用户部分

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户名字
     */
    private String userName;
    /**
     * 用户头像
     */
    private String userAvatar;

    //提交部分
    /**
     * 花费时间
     */
    private Long timeUse;
    /**
     * 花费内存
     */
    private Long memoryUse;

    /**
     * 代码长度
     */
    private String codeLength;
    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 代码语言
     */
    private String language;
    /**
     * 提交状态
     */
    private Integer status;
    /**
     * 提交状态信息
     */

    private String message;

    //题目部分

    /**
     * 题目id
     */
    private Long questionId;
    /**
     * 题目名字
     */
    private String questionTitle;

    /**
     * 判题信息，拆开成时间状态内存
     */
    private String judgeInfo;

    /**
     * 判题信息,包含用户运行结果和答案
     */
    private String judgeMessages;

    /**
     * 代码
     */
    private String code;


}

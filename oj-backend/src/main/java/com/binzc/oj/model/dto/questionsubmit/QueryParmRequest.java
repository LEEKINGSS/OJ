package com.binzc.oj.model.dto.questionsubmit;

import lombok.Data;

@Data
public class QueryParmRequest {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 题目名称
     */
    private String questionTitle;
    /**
     * 提交状态
     */
    private Integer submitStatus;
    /**
     * 语言
     */
    private String language;
    /**
     * 当前页码
     */
    private Integer pageNo=1;
    /**
     * 页码大小
     */
    private Integer pageSize=10;
}

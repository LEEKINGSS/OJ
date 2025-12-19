package com.binzc.oj.model.vo;

import lombok.Data;

import java.util.List;
@Data
public class QuestionListSafeVo {
    /**
     * 总记录数
     */
    private long totalRecords;
    /**
     * 总页数
     */
    private long totalPages;
    /**
     * 当前页码
     */
    private long currentPage;
    /**
     * 每页大小
     */
    private long pageSize;
    /**
     * 响应数据列表
     */
    private List<QuestionSafeVo> questions;
}

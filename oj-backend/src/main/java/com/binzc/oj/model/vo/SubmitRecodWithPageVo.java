package com.binzc.oj.model.vo;

import lombok.Data;

import java.util.List;
@Data
public class SubmitRecodWithPageVo {
    private List<SubmitRecordSimple> submitRecordSimples;
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
}

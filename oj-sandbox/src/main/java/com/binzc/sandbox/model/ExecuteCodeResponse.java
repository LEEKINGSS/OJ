package com.binzc.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

//    private List<String> outputList;
//
//    /**
//     * 接口信息
//     */
//    private String message;
//
//    /**
//     * 执行状态
//     */
//    private Integer status;
//
//    /**
//     * 判题信息
//     */
//    private JudgeInfo judgeInfo;
    /**
     * @Description 我觉得鱼皮这里设计的不好，应该就是一个用例一个信息的
     * 执行信息
     */
    private List<ExecuteMessage> executeMessageList;

    private int status;

    private String message;
}

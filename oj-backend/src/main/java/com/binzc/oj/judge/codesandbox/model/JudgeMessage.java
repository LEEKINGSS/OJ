package com.binzc.oj.judge.codesandbox.model;

import lombok.Data;

@Data
public class JudgeMessage {

    // 0:编译错误 1:运行错误 2:答案错误 3:超时 4:内存超限 5:格式错误 6:运行成功
    private String status;

    private String statusSingle;

    private Long time;

    private Long memory;
}

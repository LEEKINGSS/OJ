package com.binzc.oj.judge.codesandbox.model;

import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import lombok.Data;

import java.util.List;

/**
 * 这地方作者有点乱，开始数据库表没设计好，后续只能疯狂加字段
 * @author binzc
 *
 */
@Data
public class JudgeResult {
    private JudgeInfo judgeInfo;
    private List<JudgeMessage> judgeMessages;

}

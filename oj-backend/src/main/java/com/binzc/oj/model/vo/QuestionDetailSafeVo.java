package com.binzc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.binzc.oj.model.dto.question.JudgeCase;
import com.binzc.oj.model.dto.question.JudgeConfig;
import com.binzc.oj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
@Data
public class QuestionDetailSafeVo {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;


    /**
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static QuestionDetailSafeVo generateVo(Question question){
        if (question == null) {
            return null;
        }
        QuestionDetailSafeVo questionDetailSafeVo = new QuestionDetailSafeVo();
        BeanUtils.copyProperties(question, questionDetailSafeVo);
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionDetailSafeVo.setTags(tagList);
        String judgeConfigStr = question.getJudgeConfig();
        questionDetailSafeVo.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionDetailSafeVo;
    }

}

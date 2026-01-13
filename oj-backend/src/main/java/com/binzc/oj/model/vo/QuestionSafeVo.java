package com.binzc.oj.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 问题列表vo，用于展示问题页（用户）
 * @Author: binzc
 * @Date: 2025-05-12
 */
@Data
public class QuestionSafeVo {
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
     * 标签列表（json 数组）
     */
    private String tags;


    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 题目封面图片
     */
    private String coverUrl;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

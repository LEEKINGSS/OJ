package com.binzc.oj.mapper;

import com.binzc.oj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.binzc.oj.model.vo.SubmitRecordSimple;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author binzc
 * @description 针对表【question_submit(题目提交)】的数据库操作Mapper
 * @createDate 2025-05-08 13:58:41
 * @Entity com.binzc.oj.model.entity.QuestionSubmit
 */
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {
    public List<SubmitRecordSimple> getSubmitRecordSimpleList(@Param("userName") String userName,
                                                              @Param("questionTitle") String questionTitle,
                                                              @Param("submitStatus") Integer submitStatus,
                                                              @Param("language") String language,
                                                              @Param("offset") Integer offset,
                                                              @Param("pageSize") Integer pageSize);

    public long countSubmitRecordSimpleList(@Param("userName") String userName,
                                            @Param("questionTitle") String questionTitle,
                                            @Param("submitStatus") Integer submitStatus,
                                            @Param("language") String language);

    public List<SubmitRecordSimple> getSubmitRecordSimpleListById(@Param("userId") Long userId,
                                                                  @Param("questionTitle") String questionTitle,
                                                                  @Param("submitStatus") Integer submitStatus,
                                                                  @Param("language") String language,
                                                                  @Param("offset") Integer offset,
                                                                  @Param("pageSize") Integer pageSize);

    public long countSubmitRecordSimpleListById(@Param("userId") Long userId,
                                                @Param("questionTitle") String questionTitle,
                                                @Param("submitStatus") Integer submitStatus,
                                                @Param("language") String language);
}





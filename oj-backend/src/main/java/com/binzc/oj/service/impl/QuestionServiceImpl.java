package com.binzc.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.exception.ThrowUtils;
import com.binzc.oj.mapper.QuestionMapper;
import com.binzc.oj.model.vo.QuestionDetailSafeVo;
import com.binzc.oj.model.vo.QuestionListSafeVo;
import com.binzc.oj.model.vo.QuestionSafeVo;
import com.binzc.oj.service.QuestionService;
import com.binzc.oj.model.entity.Question;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author binzc
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-05-08 21:02:49
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {
    /**
     * 校验题目是否合法
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 根据条件有无分页查询数据
     * @param tags
     * @param title
     * @param page
     * @param size
     */
    @Override
    public QuestionListSafeVo getQuestionList(List<String> tags, String title, int page, int size) {
        if (page < 0 || size < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数错误");
        Page<Question> pageObj = new Page<>(page, size); // 创建分页对象
        // 创建动态查询条件
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if(title != null && !title.equals("")){
            queryWrapper.like("title", title);
        }
        // 处理标签查询
        if (tags != null && !tags.isEmpty()) {
            // 使用 OR 条件来匹配标签
            queryWrapper.and(wrapper -> {
                for (String tag : tags) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        // 判断 tags 字段是否存储为逗号分隔字符串
                        wrapper.or().like("tags", tag);
                    }
                }
            });
        }
        // 执行查询
        IPage<Question> result = this.page(pageObj, queryWrapper);
        if(result == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询失败");
        }
        // 构建响应对象
        QuestionListSafeVo questionListSafeVo = new QuestionListSafeVo();
        questionListSafeVo.setTotalRecords(result.getTotal());  // 总记录数
        questionListSafeVo.setTotalPages(result.getPages());   // 总页数
        questionListSafeVo.setCurrentPage(result.getCurrent());  // 当前页码
        questionListSafeVo.setPageSize(result.getSize());  // 每页大小
        List<QuestionSafeVo> questionSafeVoList = convertToQuestionSafeVoList(result.getRecords());
        questionListSafeVo.setQuestions(questionSafeVoList);  // 响应数据列表
        return questionListSafeVo;
    }

    public List<QuestionSafeVo> convertToQuestionSafeVoList(List<Question> questionList) {
        return questionList.stream()
                .map(question -> {
                    QuestionSafeVo safeVo = new QuestionSafeVo();
                    // 使用 BeanUtils 复制属性
                    BeanUtils.copyProperties(question, safeVo);
                    return safeVo;
                })
                .collect(Collectors.toList());
    }

    public QuestionDetailSafeVo getQuestionById(long id) {
        Question question = this.baseMapper.selectById(id);
        QuestionDetailSafeVo questionDetailSafeVo = QuestionDetailSafeVo.generateVo(question);
        return questionDetailSafeVo;
    }
}





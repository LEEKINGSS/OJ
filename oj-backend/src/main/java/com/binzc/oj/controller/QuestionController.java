package com.binzc.oj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.binzc.oj.common.BaseResponse;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.common.ResultUtils;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.exception.ThrowUtils;
import com.binzc.oj.model.dto.question.JudgeCase;
import com.binzc.oj.model.dto.question.JudgeConfig;
import com.binzc.oj.model.dto.question.QuestionAddRequest;
import com.binzc.oj.model.dto.question.QuestionUpdateRequest;
import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.model.vo.QuestionDetailSafeVo;
import com.binzc.oj.model.vo.QuestionListSafeVo;
import com.binzc.oj.service.QuestionService;
import com.binzc.oj.service.QuestionSubmitService;
import com.binzc.oj.service.UserService;
import com.binzc.oj.service.WordParseService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/question")
public class QuestionController {
    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private WordParseService wordParseService;

    @Autowired
    private QuestionSubmitService questionSubmitService;

    private final static Gson GSON = new Gson();

    /**
     * 创建新题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 根据题目id修改题目
     *
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Long> updateQuestion(@RequestBody QuestionUpdateRequest questionupdateRequest, HttpServletRequest request) {
        if (questionupdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionupdateRequest, question);
        List<String> tags = questionupdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionupdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionupdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        Long questionId = question.getId();
        return ResultUtils.success(questionId);
    }

    /**
     * 根据题目id删除题目
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = questionService.removeById(id);
        // 删除对应问题提交表中的数据
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", id);
        questionSubmitService.remove(queryWrapper);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    public BaseResponse<QuestionListSafeVo> listQuestion(@RequestParam(value = "name", required = false) String name,
                                                         @RequestParam(value = "tags", required = false) String tags,
                                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        List<String> tagList = null;
        if (tags != null && !tags.isEmpty()) {
            tagList = Arrays.asList(tags.split(","));
        }
        QuestionListSafeVo questionListSafeVo = questionService.getQuestionList(tagList, name, page, size);
        return ResultUtils.success(questionListSafeVo);

    }

    /**
     * 根据题目id获取题目
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<QuestionDetailSafeVo> getQuestionById(@PathVariable("id") long id) {
        QuestionDetailSafeVo questionDetailSafeVo = questionService.getQuestionById(id);
        return ResultUtils.success(questionDetailSafeVo);
    }

    /**
     * 解析 Word 文档为题目内容
     *
     * @param file Word 文档文件
     * @return 解析后的 Markdown 格式内容
     */
    @PostMapping("/parseWord")
    public BaseResponse<String> parseWordDocument(@RequestPart("file") MultipartFile file) {
        log.info("收到 Word 文档解析请求，文件名: {}, 文件大小: {}",
                file.getOriginalFilename(), file.getSize());
        try {
            String markdownContent = wordParseService.parseWordToMarkdown(file);
            log.info("Word 文档解析成功，内容长度: {}", markdownContent.length());
            return ResultUtils.success(markdownContent);
        } catch (Exception e) {
            log.error("Word 文档解析失败", e);
            throw e;
        }
    }
}

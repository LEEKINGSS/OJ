package com.binzc.oj.controller;

import com.binzc.oj.common.BaseResponse;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.common.ResultUtils;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.model.dto.questionsubmit.QueryParmRequest;
import com.binzc.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.judge.codesandbox.CodeSandBox;
import com.binzc.oj.judge.codesandbox.CodeSandBoxFactory;
import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.binzc.oj.judge.codesandbox.model.ExecuteMessage;
import com.binzc.oj.model.vo.*;
import com.binzc.oj.service.QuestionSubmitService;
import com.binzc.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/question/submit")
public class QuestionSubmitController {
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    @Resource
    private CodeSandBoxFactory codeSandBoxFactory;

    @Value("${codesandbox.type:remote}")
    private String codeSandBoxType;

    /**
     * 在做题页面获取到以前提交过的代码
     *
     * @param questionId
     * @param request
     * @return
     */
    @GetMapping("getCode")
    public BaseResponse<CodeVo> getCode(@RequestParam(value = "questionId", required = true) long questionId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        long userId = user.getId();
        CodeVo codeVo = questionSubmitService.getCode(questionId, userId);
        return ResultUtils.success(codeVo);
    }


    /**
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return
     */
    @PostMapping("/")
    public BaseResponse<QuestionSubmitVo> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        QuestionSubmitVo questionSubmitVo = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitVo);

    }

    @PostMapping("/submitRecord")
    public BaseResponse<SubmitRecodWithPageVo> submitRecords(@RequestBody QueryParmRequest queryParmRequest, HttpServletRequest request) {
        SubmitRecodWithPageVo submitRecodWithPageVo = questionSubmitService.getSubmitRecords(queryParmRequest, request);
        return ResultUtils.success(submitRecodWithPageVo);
    }

    @GetMapping("/record/{id}")
    public BaseResponse<SubmitRecordDetail> getRecord(@PathVariable("id") Long id, HttpServletResponse response) {
        //Todo:是否需要登录权限
        SubmitRecordDetail submitRecordDetail = questionSubmitService.getSubmitRecordDetail(id);
        return ResultUtils.success(submitRecordDetail);

    }

    /**
     * 直接运行代码（不判题）
     * @param code 代码
     * @param language 语言
     * @param inputList 输入列表（可选）
     * @return 运行结果
     */
    @PostMapping("/run")
    public BaseResponse<ExecuteCodeResponse> runCode(
            @RequestParam(value = "code", required = true) String code,
            @RequestParam(value = "language", required = true) String language,
            @RequestParam(value = "inputList", required = false) List<String> inputList) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码不能为空");
        }
        if (language == null || language.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "语言不能为空");
        }
        
        // 如果没有提供输入，使用空列表
        if (inputList == null) {
            inputList = new ArrayList<>();
        }
        
        // 创建执行请求
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        
        // 获取代码沙箱实例
        CodeSandBoxType type = CodeSandBoxType.getEnumByValue(codeSandBoxType);
        CodeSandBox codeSandBox = codeSandBoxFactory.newCodeSandBox(type);
        
        // 执行代码
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        
        return ResultUtils.success(executeCodeResponse);
    }

}

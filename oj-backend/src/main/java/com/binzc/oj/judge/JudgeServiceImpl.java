package com.binzc.oj.judge;

import cn.hutool.json.JSONUtil;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.judge.codesandbox.CodeSandBox;
import com.binzc.oj.judge.codesandbox.CodeSandBoxFactory;
import com.binzc.oj.judge.codesandbox.model.*;
import com.binzc.oj.judge.strategy.JudgeContext;
import com.binzc.oj.model.dto.question.JudgeCase;
import com.binzc.oj.model.dto.question.JudgeConfig;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.QuestionSubmit;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.service.QuestionService;
import com.binzc.oj.service.QuestionSubmitService;
import com.binzc.oj.service.UserService;
import com.binzc.oj.model.enums.QuestionSubmitStatusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private CodeSandBoxFactory codeSandBoxFactory;

    @Value("${codesandbox.type:remote}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //获取题目提交对象，做一些处理
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目提交信息不存在");
        }
        //获取题目信息，做一些处理
        long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目信息不存在");
        }
        // 获取提交人信息，做一些处理
        long userId = questionSubmit.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户信息不存在");
        }
        // 避免一直提交判题
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        //根据配置文件，获得相应的沙箱
        CodeSandBoxType codeSandBoxType = CodeSandBoxType.getEnumByValue(type);
        CodeSandBox codeSandBox = codeSandBoxFactory.newCodeSandBox(codeSandBoxType);
        // 构建判题机输入用例
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        int retryCount = 0;
        int maxRetry = 5;
        ExecuteCodeResponse executeCodeResponse = null;

        do {
            executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
            retryCount++;
            try {
                if (executeCodeResponse == null) {
                    Thread.sleep(200); // 加一点点延迟，避免空转
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("线程中断异常");
            }
        } while (executeCodeResponse == null && retryCount < maxRetry);

        // 没办法咯，判题一直失败，可能是服务挂了
        if (executeCodeResponse == null) {
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.PANIC.getValue());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("判题机器异常！！！\n抱歉，请再次提交，代码我们做了保存，只需在题目页面再次提交即可");
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            update = questionSubmitService.updateById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行代码失败，已重试多次");
        }
        //成功，开始判题
        else if (executeCodeResponse.getStatus() == 0 && executeCodeResponse.getExecuteMessageList() != null) {
            List<ExecuteMessage> executeMessages = executeCodeResponse.getExecuteMessageList();
            List<String> outputList = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
            JudgeContext judgeContext = new JudgeContext();
            judgeContext.setExecuteMessages(executeMessages);
            judgeContext.setOutputList(outputList);
            judgeContext.setLanguage(language);
            JudgeConfig judgeConfig = JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class);
            judgeContext.setJudgeConfig(judgeConfig);
            JudgeResult judgeResult = judgeManager.doJudge(judgeContext);
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            JudgeInfo judgeInfo = judgeResult.getJudgeInfo();
            // 更新题目数据库信息
            Question questionUpdate = new Question();
            questionUpdate.setId(question.getId());
            questionUpdate.setSubmitNum(question.getSubmitNum() + 1);
            if ("Accepted".equals(judgeInfo.getMessage())) {
                questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
                questionUpdate.setAcceptedNum(question.getAcceptedNum() + 1);
            } else {
                questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            }
            boolean updateQuestion = questionService.updateById(questionUpdate);
            judgeInfo.setMessage(executeCodeResponse.getMessage());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            questionSubmitUpdate.setJudgeMessages(JSONUtil.toJsonStr(judgeResult.getJudgeMessages()));
            update = questionSubmitService.updateById(questionSubmitUpdate);
            if (!update || !updateQuestion) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
            }
            QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
            return questionSubmitResult;

        }
        //编译错误
        else if (executeCodeResponse.getStatus() != 0 && executeCodeResponse.getExecuteMessageList() == null) {
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMemory(null);
            judgeInfo.setTime(null);
            judgeInfo.setMessage(executeCodeResponse.getMessage());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            update = questionSubmitService.updateById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行代码失败，已重试多次");
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "作者写代码不严谨的错误");
        }
    }
}

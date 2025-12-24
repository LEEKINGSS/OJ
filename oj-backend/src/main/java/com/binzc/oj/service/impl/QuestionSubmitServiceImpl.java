package com.binzc.oj.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.judge.JudgeService;
import com.binzc.oj.mapper.QuestionSubmitMapper;
import com.binzc.oj.model.dto.questionsubmit.JudgeInfo;
import com.binzc.oj.model.dto.questionsubmit.QueryParmRequest;
import com.binzc.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.binzc.oj.model.entity.Question;
import com.binzc.oj.model.entity.User;
import com.binzc.oj.model.enums.QuestionSubmitLanguageEnum;
import com.binzc.oj.model.enums.QuestionSubmitStatusEnum;
import com.binzc.oj.model.vo.*;
import com.binzc.oj.service.QuestionService;
import com.binzc.oj.service.QuestionSubmitService;
import com.binzc.oj.model.entity.QuestionSubmit;

import com.binzc.oj.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.binzc.oj.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author binzc
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2025-05-08 21:02:49
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {
    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private QuestionService questionService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Override
    public CodeVo getCode(long questionId, long userId) {
        // 查询条件
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId)
                .eq("userId", userId).orderByDesc("updateTime")
                .last("limit 1");
        // 执行查询
        QuestionSubmit submit = this.getOne(queryWrapper);
        if (submit == null) {
            throw new RuntimeException("没有找到该题目的提交记录");
        }
        CodeVo codeVo = new CodeVo();
        codeVo.setCode(submit.getCode());
        codeVo.setLanguage(submit.getLanguage());
        return codeVo;
    }

    @Override
    public QuestionSubmitVo doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User user) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = user.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("正在排队中...,请稍后");
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 执行判题服务
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        QuestionSubmitVo questionSubmitVo = QuestionSubmitVo.generateVo(questionSubmit, question, user);
        return questionSubmitVo;
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = userService.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    @Override
    public SubmitRecodWithPageVo getSubmitRecords(QueryParmRequest queryParmRequest, HttpServletRequest request) {
        // 判断当前登录的是否是管理员
        User currentUser = getLoginUser(request);
        if (Objects.equals(currentUser.getUserRole(), "admin")) {
            String userName = queryParmRequest.getUserName();
            String questionTitle = queryParmRequest.getQuestionTitle();
            Integer submitStatus = queryParmRequest.getSubmitStatus();
            String language = "全部语言".equals(queryParmRequest.getLanguage()) ? "" : queryParmRequest.getLanguage();
            Integer pageNo = queryParmRequest.getPageNo();
            Integer pageSize = queryParmRequest.getPageSize();
            Integer offset = (pageNo - 1) * pageSize;
            List<SubmitRecordSimple> submitRecordSimples = questionSubmitMapper.getSubmitRecordSimpleList(userName, questionTitle, submitStatus, language, offset, pageSize);
            for (SubmitRecordSimple submitRecordSimple : submitRecordSimples) {
                JudgeInfo judgeInfo = JSONUtil.toBean(submitRecordSimple.getJudgeInfo(), JudgeInfo.class);
                submitRecordSimple.setMemoryUse(judgeInfo.getMemory());
                submitRecordSimple.setTimeUse(judgeInfo.getTime());
                submitRecordSimple.setMessage(QuestionSubmitStatusEnum.getEnumByValue(submitRecordSimple.getStatus()).getText());
            }
            Long total = questionSubmitMapper.countSubmitRecordSimpleList(userName, questionTitle, submitStatus, language);
            SubmitRecodWithPageVo submitRecodWithPageVo = new SubmitRecodWithPageVo();
            submitRecodWithPageVo.setPageNo(pageNo);
            submitRecodWithPageVo.setPageSize(pageSize);
            submitRecodWithPageVo.setTotal(total);
            submitRecodWithPageVo.setSubmitRecordSimples(submitRecordSimples);
            return submitRecodWithPageVo;
        }else if(Objects.equals(currentUser.getUserRole(), "user")){
            Long userId = currentUser.getId();
            String questionTitle = queryParmRequest.getQuestionTitle();
            Integer submitStatus = queryParmRequest.getSubmitStatus();
            String language = "全部语言".equals(queryParmRequest.getLanguage()) ? "" : queryParmRequest.getLanguage();
            Integer pageNo = queryParmRequest.getPageNo();
            Integer pageSize = queryParmRequest.getPageSize();
            Integer offset = (pageNo - 1) * pageSize;
            List<SubmitRecordSimple> submitRecordSimples = questionSubmitMapper.getSubmitRecordSimpleListById(userId, questionTitle, submitStatus, language, offset, pageSize);
            for (SubmitRecordSimple submitRecordSimple : submitRecordSimples) {
                JudgeInfo judgeInfo = JSONUtil.toBean(submitRecordSimple.getJudgeInfo(), JudgeInfo.class);
                submitRecordSimple.setMemoryUse(judgeInfo.getMemory());
                submitRecordSimple.setTimeUse(judgeInfo.getTime());
                submitRecordSimple.setMessage(QuestionSubmitStatusEnum.getEnumByValue(submitRecordSimple.getStatus()).getText());
            }
            Long total = questionSubmitMapper.countSubmitRecordSimpleListById(userId, questionTitle, submitStatus, language);
            SubmitRecodWithPageVo submitRecodWithPageVo = new SubmitRecodWithPageVo();
            submitRecodWithPageVo.setPageNo(pageNo);
            submitRecodWithPageVo.setPageSize(pageSize);
            submitRecodWithPageVo.setTotal(total);
            submitRecodWithPageVo.setSubmitRecordSimples(submitRecordSimples);
            return submitRecodWithPageVo;
        }else{
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Override
    public SubmitRecordDetail getSubmitRecordDetail(long submitId) {
        QuestionSubmit questionSubmit = this.baseMapper.selectById(submitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        Long userId = questionSubmit.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        SubmitRecordDetail submitRecordDetail = SubmitRecordDetail.generateVo(user, questionSubmit, question);
        return submitRecordDetail;
    }


}





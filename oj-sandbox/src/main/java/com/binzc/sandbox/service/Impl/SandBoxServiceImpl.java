package com.binzc.sandbox.service.Impl;

import com.binzc.sandbox.common.ErrorCode;
import com.binzc.sandbox.exception.BusinessException;
import com.binzc.sandbox.executor.CodeExecutor;
import com.binzc.sandbox.model.ExecuteCodeRequest;
import com.binzc.sandbox.model.ExecuteCodeResponse;
import com.binzc.sandbox.model.LanguageEnum;
import com.binzc.sandbox.service.SandBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SandBoxServiceImpl implements SandBoxService {
    private final Map<LanguageEnum, CodeExecutor> executorMap;

    @Autowired
    public SandBoxServiceImpl(List<CodeExecutor> executors) {
        this.executorMap = executors.stream()
                .collect(Collectors.toMap(
                        CodeExecutor::language,
                        e -> e,
                        (e1, e2) -> {
                            throw new IllegalStateException("Duplicate executor for language: " + e1.language());
                        }
                ));
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        LanguageEnum language;
        try {
            language = LanguageEnum.fromValue(executeCodeRequest.getLanguage());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"语言不支持");
        }

        CodeExecutor executor = executorMap.get(language);
        if (executor == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"语言不支持");
        }

        return executor.execute(executeCodeRequest);

    }
}

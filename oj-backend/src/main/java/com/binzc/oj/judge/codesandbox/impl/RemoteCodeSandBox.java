package com.binzc.oj.judge.codesandbox.impl;

import com.binzc.oj.common.BaseResponse;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.judge.codesandbox.CodeSandBox;
import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.binzc.oj.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
public class RemoteCodeSandBox implements CodeSandBox {
    @Value("${codesandbox.url:http://localhost:8122/api/code/executeCode}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExecuteCodeRequest> entity = new HttpEntity<>(executeCodeRequest, headers);

        try {
            ResponseEntity<BaseResponse<ExecuteCodeResponse>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<BaseResponse<ExecuteCodeResponse>>() {}
            );
            BaseResponse<ExecuteCodeResponse> baseResponse = response.getBody();
            if (baseResponse.getCode()==0){
                return baseResponse.getData();
            }else {
                // 判题不成功外层会循环，不成功是不可能的
                return null;
            }
            // ... 原代码
        } catch (Exception e) {
            return null;
        }


    }

    @Override
    public CodeSandBoxType getCodeSandBoxType() {
        return CodeSandBoxType.REMOTE;
    }
}

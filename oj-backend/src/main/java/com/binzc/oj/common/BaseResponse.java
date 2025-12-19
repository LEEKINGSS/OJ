package com.binzc.oj.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @Description: 通用返回对象
 * @Author binzc
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }





}

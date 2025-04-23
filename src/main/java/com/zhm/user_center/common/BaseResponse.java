package com.zhm.user_center.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int Code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        Code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, null);
    }

    public BaseResponse(int code, T data) {
        this(code, data, null, null);
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}

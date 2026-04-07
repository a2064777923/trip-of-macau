package com.aoxiaoyou.tripofmacau.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(4000, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

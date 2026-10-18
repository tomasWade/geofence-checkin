package com.geofence.checkin.common.exception;

import lombok.Getter;

/**
 * 业务异常，携带错误码与提示信息，由全局异常处理器统一转为响应体。
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

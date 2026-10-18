package com.geofence.checkin.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一响应体 {code, msg, data}，code=0 表示成功。
 */
@Data
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "ok", data);
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(0, "ok", null);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        return new ApiResult<>(code, msg, null);
    }
}

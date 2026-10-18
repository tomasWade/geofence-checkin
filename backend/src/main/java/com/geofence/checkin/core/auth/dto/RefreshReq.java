package com.geofence.checkin.core.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求体。
 */
@Data
public class RefreshReq {
    @NotBlank
    private String refreshToken;
}

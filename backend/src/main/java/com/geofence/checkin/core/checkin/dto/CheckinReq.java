package com.geofence.checkin.core.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 打卡请求体：端上坐标与精度（user_id 一律取 JWT，不受理请求体）。
 */
@Data
public class CheckinReq {
    @NotNull
    private BigDecimal lng;
    @NotNull
    private BigDecimal lat;
    private Double accuracyM;
    private String clientTime;
}

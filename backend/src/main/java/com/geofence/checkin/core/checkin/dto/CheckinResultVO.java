package com.geofence.checkin.core.checkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打卡结果视图：命中围栏、复算距离、服务端时间。
 */
@Data
@Builder
@AllArgsConstructor
public class CheckinResultVO {
    private Long recordId;
    private Long fenceId;
    private String fenceName;
    private Double distanceM;
    private BigDecimal lng;
    private BigDecimal lat;
    private LocalDateTime checkinTime;
    private String message;
}

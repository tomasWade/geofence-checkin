package com.geofence.checkin.biz.report.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日报视图对象。
 */
@Data
public class DailyReportVO {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate checkinDate;
    private LocalDateTime firstInTime;
    private LocalDateTime lastOutTime;
    private Integer workMinutes;
    private Integer statusEnum;
    private String statusText;
}

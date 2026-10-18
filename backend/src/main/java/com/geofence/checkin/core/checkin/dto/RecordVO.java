package com.geofence.checkin.core.checkin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录视图对象（含人名/围栏名/代打卡人）。
 */
@Data
public class RecordVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long fenceId;
    private String fenceName;
    private LocalDate checkinDate;
    private LocalDateTime checkinTime;
    private LocalDateTime clientTime;
    private Double distanceM;
    private Double accuracyM;
    private java.math.BigDecimal lng;
    private java.math.BigDecimal lat;
    private String source;
    private Integer suspicious;
    private String remark;
    private Long manualBy;
    private String manualByName;
}

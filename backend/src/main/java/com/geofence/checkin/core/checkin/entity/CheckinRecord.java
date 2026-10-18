package com.geofence.checkin.core.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡事实记录（不可变事实：谁/何时/何地/距哪个围栏多远，判定结果不写回）。
 */
@Data
@TableName("checkin_record")
public class CheckinRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fenceId;
    private LocalDate checkinDate;
    private LocalDateTime checkinTime;
    private LocalDateTime clientTime;
    private BigDecimal lng;
    private BigDecimal lat;
    private Double accuracyM;
    private Double distanceM;
    private String source;
    private Long manualBy;
    private Integer suspicious;
    private String remark;
}

package com.geofence.checkin.biz.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日报投影实体（可删除重建，判定结果不回写打卡事实）。
 */
@Data
@TableName("daily_report")
public class DailyReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate checkinDate;
    private LocalDateTime firstInTime;
    private LocalDateTime lastOutTime;
    private Integer workMinutes;
    private Integer statusEnum;
}

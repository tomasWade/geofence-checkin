package com.geofence.checkin.biz.attend.service;

import com.geofence.checkin.biz.report.entity.DailyReport;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 固定班次判定参考实现：迟到/早退/缺卡/旷工/地点异常 bitmap（1/2/4/8/16），工时=末卡-首卡。
 */
@Component
public class FixedShiftEvaluator implements AttendanceEvaluator {

    public static final int LATE = 1;
    public static final int EARLY_LEAVE = 2;
    public static final int MISSING_PUNCH = 4;
    public static final int ABSENT = 8;
    public static final int WRONG_PLACE = 16;

    @Override
    public DailyReport evaluate(Long userId, LocalDate date, CheckinRule rule, List<CheckinRecord> records) {
        DailyReport report = new DailyReport();
        report.setUserId(userId);
        report.setCheckinDate(date);

        if (records == null || records.isEmpty()) {
            report.setStatusEnum(ABSENT);
            return report;
        }

        LocalDateTime first = records.stream().map(CheckinRecord::getCheckinTime).min(LocalDateTime::compareTo).orElseThrow();
        LocalDateTime last = records.stream().map(CheckinRecord::getCheckinTime).max(LocalDateTime::compareTo).orElseThrow();
        report.setFirstInTime(first);
        report.setLastOutTime(last);
        report.setWorkMinutes((int) Duration.between(first, last).toMinutes());

        int status = 0;
        if (first.equals(last)) {
            status |= MISSING_PUNCH;
        } else {
            int firstOff = toDayOffset(first);
            int lastOff = toDayOffset(last);
            int tolerance = rule.getLateToleranceSec() == null ? 0 : rule.getLateToleranceSec();
            if (firstOff > rule.getWorkStartSec() + tolerance) {
                status |= LATE;
            }
            if (lastOff < rule.getWorkEndSec()) {
                status |= EARLY_LEAVE;
            }
        }
        boolean wrongPlace = records.stream()
                .anyMatch(r -> !rule.getFenceId().equals(r.getFenceId()));
        if (wrongPlace) {
            status |= WRONG_PLACE;
        }
        report.setStatusEnum(status);
        return report;
    }

    private int toDayOffset(LocalDateTime time) {
        return time.getHour() * 3600 + time.getMinute() * 60 + time.getSecond();
    }
}

package com.geofence.checkin.biz.attend.service;

import com.geofence.checkin.biz.report.entity.DailyReport;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤判定器接口（二开扩展点）：输入(用户, 日期, 规则, 当日记录) -> 日报。
 */
public interface AttendanceEvaluator {

    DailyReport evaluate(Long userId, LocalDate date, CheckinRule rule, List<CheckinRecord> records);
}

package com.geofence.checkin.biz.attend.service;

import com.geofence.checkin.biz.report.entity.DailyReport;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 固定班次判定矩阵单测：旷工/缺卡/迟到/宽限临界/早退/组合状态/地点异常/工时。
 */
class FixedShiftEvaluatorTest {

    private final FixedShiftEvaluator evaluator = new FixedShiftEvaluator();
    private final LocalDate date = LocalDate.of(2026, 10, 16);

    private CheckinRule rule(int startSec, int endSec, int toleranceSec, long fenceId) {
        CheckinRule r = new CheckinRule();
        r.setId(1L);
        r.setWorkStartSec(startSec);
        r.setWorkEndSec(endSec);
        r.setLateToleranceSec(toleranceSec);
        r.setFenceId(fenceId);
        return r;
    }

    private CheckinRecord record(int hour, int minute, long fenceId) {
        CheckinRecord r = new CheckinRecord();
        r.setUserId(7L);
        r.setCheckinTime(date.atTime(hour, minute));
        r.setFenceId(fenceId);
        r.setDistanceM(50.0);
        r.setLng(BigDecimal.ONE);
        r.setLat(BigDecimal.ONE);
        return r;
    }

    @Test
    void noRecordsMeansAbsent() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1), List.of());
        assertEquals(FixedShiftEvaluator.ABSENT, report.getStatusEnum());
        assertNull(report.getFirstInTime());
    }

    @Test
    void nullRecordsMeansAbsent() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1), null);
        assertEquals(FixedShiftEvaluator.ABSENT, report.getStatusEnum());
    }

    @Test
    void singlePunchMeansMissingPunch() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(9, 0, 1)));
        assertEquals(FixedShiftEvaluator.MISSING_PUNCH, report.getStatusEnum());
    }

    @Test
    void normalDayHasNoFlags() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(8, 50, 1), record(18, 10, 1)));
        assertEquals(0, report.getStatusEnum());
        assertEquals(560, report.getWorkMinutes());
    }

    @Test
    void lateWhenFirstCardAfterStartPlusTolerance() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 300, 1),
                List.of(record(9, 6, 1), record(18, 0, 1)));
        assertEquals(FixedShiftEvaluator.LATE, report.getStatusEnum());
    }

    @Test
    void exactlyAtToleranceBoundaryIsNotLate() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 300, 1),
                List.of(record(9, 5, 1), record(18, 0, 1)));
        assertEquals(0, report.getStatusEnum());
    }

    @Test
    void earlyLeaveWhenLastCardBeforeEnd() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(9, 0, 1), record(17, 30, 1)));
        assertEquals(FixedShiftEvaluator.EARLY_LEAVE, report.getStatusEnum());
    }

    @Test
    void lateAndEarlyLeaveCombined() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(9, 30, 1), record(17, 0, 1)));
        assertEquals(FixedShiftEvaluator.LATE | FixedShiftEvaluator.EARLY_LEAVE, report.getStatusEnum());
    }

    @Test
    void wrongFenceMarksWrongPlace() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(8, 50, 2), record(18, 0, 2)));
        assertEquals(FixedShiftEvaluator.WRONG_PLACE, report.getStatusEnum());
    }

    @Test
    void mixedFencesStillWrongPlace() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(8, 50, 1), record(18, 0, 2)));
        assertEquals(FixedShiftEvaluator.WRONG_PLACE, report.getStatusEnum());
    }

    @Test
    void latePlusWrongPlaceCombined() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(9, 30, 2), record(18, 0, 1)));
        assertEquals(FixedShiftEvaluator.LATE | FixedShiftEvaluator.WRONG_PLACE, report.getStatusEnum());
    }

    @Test
    void workMinutesIsLastMinusFirst() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(8, 0, 1), record(12, 0, 1), record(18, 0, 1)));
        assertEquals(600, report.getWorkMinutes());
        assertEquals(date.atTime(8, 0), report.getFirstInTime());
        assertEquals(date.atTime(18, 0), report.getLastOutTime());
    }

    @Test
    void multipleRecordsUnorderedStillCorrect() {
        DailyReport report = evaluator.evaluate(7L, date, rule(9 * 3600, 18 * 3600, 0, 1),
                List.of(record(18, 0, 1), record(8, 50, 1), record(12, 0, 1)));
        assertEquals(date.atTime(8, 50), report.getFirstInTime());
        assertEquals(date.atTime(18, 0), report.getLastOutTime());
    }
}

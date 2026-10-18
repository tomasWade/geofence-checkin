package com.geofence.checkin.biz.attend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.biz.report.entity.DailyReport;
import com.geofence.checkin.biz.report.mapper.DailyReportMapper;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.system.audit.Audit;
import com.geofence.checkin.core.system.entity.SysUser;
import com.geofence.checkin.core.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 日报重算：按日期区间对全部启用用户重判并重建 daily_report 投影（先清理无规则用户的旧报告；区间上限 62 天且不晚于今天）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RebuildService {

    private static final int MAX_RANGE_DAYS = 62;

    private final SysUserMapper userMapper;
    private final RuleMatchService ruleMatchService;
    private final AttendanceEvaluator evaluator;
    private final CheckinRecordMapper recordMapper;
    private final DailyReportMapper dailyReportMapper;

    @Audit(action = "report:rebuild")
    @Transactional
    public int rebuild(LocalDate begin, LocalDate end) {
        if (end.isBefore(begin)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (end.isAfter(LocalDate.now())) {
            throw new BusinessException("结束日期不能晚于今天");
        }
        if (begin.plusDays(MAX_RANGE_DAYS - 1).isBefore(end)) {
            throw new BusinessException("重算区间不能超过 " + MAX_RANGE_DAYS + " 天");
        }
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1));
        int count = 0;
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            for (SysUser user : users) {
                Optional<CheckinRule> ruleOpt = ruleMatchService.match(user.getId(), date);
                if (ruleOpt.isEmpty()) {
                    dailyReportMapper.delete(new LambdaQueryWrapper<DailyReport>()
                            .eq(DailyReport::getUserId, user.getId())
                            .eq(DailyReport::getCheckinDate, date));
                    continue;
                }
                List<CheckinRecord> records = recordMapper.selectList(
                        new LambdaQueryWrapper<CheckinRecord>()
                                .eq(CheckinRecord::getUserId, user.getId())
                                .eq(CheckinRecord::getCheckinDate, date));
                DailyReport report = evaluator.evaluate(user.getId(), date, ruleOpt.get(), records);
                dailyReportMapper.delete(new LambdaQueryWrapper<DailyReport>()
                        .eq(DailyReport::getUserId, user.getId())
                        .eq(DailyReport::getCheckinDate, date));
                dailyReportMapper.insert(report);
                count++;
            }
        }
        log.info("daily report rebuilt from {} to {}, {} rows", begin, end, count);
        return count;
    }
}

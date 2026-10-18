package com.geofence.checkin.biz.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.biz.report.dto.DailyReportVO;
import com.geofence.checkin.biz.report.entity.DailyReport;
import com.geofence.checkin.biz.report.mapper.DailyReportMapper;
import com.geofence.checkin.core.system.service.DataScopeService;
import com.geofence.checkin.core.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 日报查询：数据范围过滤 + 用户名映射 + 状态文本化。
 */
@Service
@RequiredArgsConstructor
public class ReportQueryService {

    private final DailyReportMapper dailyReportMapper;
    private final UserService userService;
    private final DataScopeService dataScopeService;

    public Page<DailyReportVO> page(long page, long size, LocalDate begin, LocalDate end, Long userId) {
        Set<Long> visible = dataScopeService.visibleUserIds();
        Page<DailyReport> result = dailyReportMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<DailyReport>()
                        .ge(begin != null, DailyReport::getCheckinDate, begin)
                        .le(end != null, DailyReport::getCheckinDate, end)
                        .eq(userId != null, DailyReport::getUserId, userId)
                        .in(visible != null, DailyReport::getUserId, visible)
                        .orderByDesc(DailyReport::getCheckinDate)
                        .orderByAsc(DailyReport::getUserId));
        Page<DailyReportVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        if (!result.getRecords().isEmpty()) {
            ArrayList<Long> ids = new ArrayList<>();
            result.getRecords().forEach(r -> ids.add(r.getUserId()));
            Map<Long, String> names = userService.nameMap(ids);
            voPage.setRecords(result.getRecords().stream().map(r -> {
                DailyReportVO vo = new DailyReportVO();
                vo.setId(r.getId());
                vo.setUserId(r.getUserId());
                vo.setUserName(names.get(r.getUserId()));
                vo.setCheckinDate(r.getCheckinDate());
                vo.setFirstInTime(r.getFirstInTime());
                vo.setLastOutTime(r.getLastOutTime());
                vo.setWorkMinutes(r.getWorkMinutes());
                vo.setStatusEnum(r.getStatusEnum());
                vo.setStatusText(toText(r.getStatusEnum()));
                return vo;
            }).toList());
        } else {
            voPage.setRecords(List.of());
        }
        return voPage;
    }

    private String toText(int status) {
        if (status == 0) {
            return "正常";
        }
        StringBuilder sb = new StringBuilder();
        if ((status & 1) != 0) sb.append("迟到 ");
        if ((status & 2) != 0) sb.append("早退 ");
        if ((status & 4) != 0) sb.append("缺卡 ");
        if ((status & 8) != 0) sb.append("旷工 ");
        if ((status & 16) != 0) sb.append("地点异常");
        return sb.toString().trim();
    }
}

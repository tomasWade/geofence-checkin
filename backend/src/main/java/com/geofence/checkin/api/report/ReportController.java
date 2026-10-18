package com.geofence.checkin.api.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.biz.attend.service.RebuildService;
import com.geofence.checkin.biz.report.dto.DailyReportVO;
import com.geofence.checkin.biz.report.service.ReportQueryService;
import com.geofence.checkin.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 报表接口：日报查询与区间重算。
 */
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final RebuildService rebuildService;

    @GetMapping("/daily")
    @PreAuthorize("@ss.hasPermi('report:daily')")
    public ApiResult<Page<DailyReportVO>> daily(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                @RequestParam(required = false) Long userId) {
        return ApiResult.ok(reportQueryService.page(page, size, begin, end, userId));
    }

    @PostMapping("/rebuild")
    @PreAuthorize("@ss.hasPermi('report:rebuild')")
    public ApiResult<Map<String, Integer>> rebuild(@RequestParam
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
                                                   @RequestParam
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (end.isBefore(begin)) {
            return ApiResult.fail(400, "结束日期不能早于开始日期");
        }
        return ApiResult.ok(Map.of("rows", rebuildService.rebuild(begin, end)));
    }
}

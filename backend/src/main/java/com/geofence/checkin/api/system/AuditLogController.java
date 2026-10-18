package com.geofence.checkin.api.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.system.entity.SysAuditLog;
import com.geofence.checkin.core.system.service.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志查询接口。
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditQueryService auditQueryService;

    @GetMapping("/logs")
    @PreAuthorize("@ss.hasPermi('audit:list')")
    public ApiResult<Page<SysAuditLog>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) String begin,
                                             @RequestParam(required = false) String end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime beginTime;
        LocalDateTime endTime;
        try {
            beginTime = begin == null ? null : LocalDateTime.parse(begin, fmt);
            endTime = end == null ? null : LocalDateTime.parse(end, fmt);
        } catch (Exception e) {
            return ApiResult.fail(400, "时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
        return ApiResult.ok(auditQueryService.page(page, size, beginTime, endTime));
    }
}

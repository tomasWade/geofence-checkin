package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.core.system.entity.SysAuditLog;
import com.geofence.checkin.core.system.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志分页查询。
 */
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final SysAuditLogMapper auditLogMapper;

    public Page<SysAuditLog> page(long page, long size, LocalDateTime begin, LocalDateTime end) {
        return auditLogMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<SysAuditLog>()
                        .ge(begin != null, SysAuditLog::getCreatedAt, begin)
                        .le(end != null, SysAuditLog::getCreatedAt, end)
                        .orderByDesc(SysAuditLog::getId));
    }
}

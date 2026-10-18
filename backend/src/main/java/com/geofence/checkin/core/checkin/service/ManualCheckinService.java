package com.geofence.checkin.core.checkin.service;

import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.checkin.dto.ManualCheckinReq;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import com.geofence.checkin.core.system.audit.Audit;
import com.geofence.checkin.core.system.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员代打卡：独立权限码 + 数据范围校验 + 禁止补录未来时间 + 强制备注，source=MANUAL 可疑标记。
 */
@Service
@RequiredArgsConstructor
public class ManualCheckinService {

    private final CheckinRecordMapper recordMapper;
    private final GeoFenceMapper fenceMapper;
    private final DataScopeService dataScopeService;

    @Audit(action = "checkin:manual")
    public Long create(ManualCheckinReq req) {
        dataScopeService.checkAccess(req.getUserId());
        GeoFence fence = fenceMapper.selectById(req.getFenceId());
        if (fence == null) {
            throw new BusinessException("围栏不存在");
        }
        if (req.getCheckinTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("不能补录未来时间");
        }
        CheckinRecord record = new CheckinRecord();
        record.setUserId(req.getUserId());
        record.setFenceId(req.getFenceId());
        record.setCheckinDate(req.getCheckinTime().toLocalDate());
        record.setCheckinTime(req.getCheckinTime());
        record.setLng(fence.getLng());
        record.setLat(fence.getLat());
        record.setDistanceM(0.0);
        record.setSource("MANUAL");
        record.setManualBy(CurrentUser.requireUserId());
        record.setSuspicious(1);
        record.setRemark(req.getRemark());
        recordMapper.insert(record);
        return record.getId();
    }
}

package com.geofence.checkin.core.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.geo.calc.Haversine;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡服务实现：服务端时间 + Haversine 复算，命中任一启用围栏（重叠取最近）即落库，全不命中拒绝并提示最近围栏距离；精度差仅作可疑标记。
 */
@Service
@RequiredArgsConstructor
public class RealCheckinService implements CheckinService {

    private final CheckinRecordMapper recordMapper;
    private final GeoFenceMapper fenceMapper;

    @Override
    public CheckinResultVO doCheckin(Long userId, CheckinReq req) {
        LocalDateTime now = LocalDateTime.now();
        List<GeoFence> fences = fenceMapper.selectList(
                new LambdaQueryWrapper<GeoFence>()
                        .eq(GeoFence::getStatus, 1)
                        .orderByAsc(GeoFence::getId));

        GeoFence hit = null;
        double hitDistance = Double.MAX_VALUE;
        GeoFence nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (GeoFence fence : fences) {
            double d = Haversine.distanceMeters(
                    req.getLng().doubleValue(), req.getLat().doubleValue(),
                    fence.getLng().doubleValue(), fence.getLat().doubleValue());
            if (d < nearestDistance) {
                nearest = fence;
                nearestDistance = d;
            }
            if (d <= fence.getRadiusM() && d < hitDistance) {
                hit = fence;
                hitDistance = d;
            }
        }

        if (hit == null) {
            String near = nearest != null
                    ? String.format("，距离最近围栏「%s」约 %.0f 米", nearest.getName(), nearestDistance)
                    : "";
            throw new BusinessException("不在任何打卡围栏内" + near);
        }

        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setFenceId(hit.getId());
        record.setCheckinDate(now.toLocalDate());
        record.setCheckinTime(now);
        if (req.getClientTime() != null && !req.getClientTime().isBlank()) {
            try {
                record.setClientTime(LocalDateTime.parse(req.getClientTime().replace(' ', 'T')));
            } catch (Exception ignored) {
            }
        }
        record.setLng(req.getLng());
        record.setLat(req.getLat());
        record.setAccuracyM(req.getAccuracyM());
        record.setDistanceM(hitDistance);
        boolean suspicious = req.getAccuracyM() != null && req.getAccuracyM() > 200;
        record.setSuspicious(suspicious ? 1 : 0);
        record.setSource("H5");
        recordMapper.insert(record);

        return CheckinResultVO.builder()
                .recordId(record.getId())
                .fenceId(hit.getId())
                .fenceName(hit.getName())
                .distanceM(hitDistance)
                .lng(req.getLng())
                .lat(req.getLat())
                .checkinTime(now)
                .message("打卡成功")
                .build();
    }
}

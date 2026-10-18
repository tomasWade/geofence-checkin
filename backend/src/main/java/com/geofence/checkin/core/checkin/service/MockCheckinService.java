package com.geofence.checkin.core.checkin.service;

import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡 mock 实现（checkin.mock-enabled=true 时生效）：固定成功记录/模拟圈外拒绝，source=MOCK。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "checkin.mock-enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MockCheckinService implements CheckinService {

    private final CheckinRecordMapper recordMapper;
    private final GeoFenceMapper fenceMapper;

    @Override
    public CheckinResultVO doCheckin(Long userId, CheckinReq req) {
        log.info("MOCK checkin userId={} lng={} lat={}", userId, req.getLng(), req.getLat());

        if (Boolean.TRUE.equals(req.getMockOutside())) {
            throw new BusinessException("不在任何打卡围栏内，距离最近围栏 421.3 米（mock）");
        }

        List<GeoFence> fences = fenceMapper.selectList(null);
        GeoFence fence = fences.isEmpty() ? null : fences.get(0);
        LocalDateTime now = LocalDateTime.now();
        Double distance = 86.5;

        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setFenceId(fence != null ? fence.getId() : 1L);
        record.setCheckinDate(LocalDate.now());
        record.setCheckinTime(now);
        record.setLng(req.getLng() != null ? req.getLng() : new BigDecimal("104.065735"));
        record.setLat(req.getLat() != null ? req.getLat() : new BigDecimal("30.657008"));
        record.setAccuracyM(req.getAccuracyM());
        record.setDistanceM(distance);
        record.setSource("MOCK");
        record.setSuspicious(0);
        recordMapper.insert(record);

        return CheckinResultVO.builder()
                .recordId(record.getId())
                .fenceId(record.getFenceId())
                .fenceName(fence != null ? fence.getName() : "示例围栏(mock)")
                .distanceM(distance)
                .lng(record.getLng())
                .lat(record.getLat())
                .checkinTime(now)
                .message("打卡成功（mock）")
                .build();
    }
}

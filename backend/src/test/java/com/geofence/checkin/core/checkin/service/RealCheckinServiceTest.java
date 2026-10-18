package com.geofence.checkin.core.checkin.service;

import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 真实打卡判定单测：圈内命中/圈外拒绝（含最近围栏提示）/重叠围栏取最近/精度可疑标记/落库字段。
 */
class RealCheckinServiceTest {

    private final CheckinRecordMapper recordMapper = mock(CheckinRecordMapper.class);
    private final GeoFenceMapper fenceMapper = mock(GeoFenceMapper.class);
    private final RealCheckinService service = new RealCheckinService(recordMapper, fenceMapper);

    private GeoFence fence(long id, String name, double lng, double lat, int radius) {
        GeoFence f = new GeoFence();
        f.setId(id);
        f.setName(name);
        f.setLng(BigDecimal.valueOf(lng));
        f.setLat(BigDecimal.valueOf(lat));
        f.setRadiusM(radius);
        f.setStatus(1);
        return f;
    }

    private CheckinReq req(double lng, double lat, Double accuracy) {
        CheckinReq r = new CheckinReq();
        r.setLng(BigDecimal.valueOf(lng));
        r.setLat(BigDecimal.valueOf(lat));
        r.setAccuracyM(accuracy);
        return r;
    }

    @Test
    void insideFenceSucceedsAndPersistsFact() {
        when(fenceMapper.selectList(any())).thenReturn(List.of(fence(1, "总部", 104.065735, 30.657008, 200)));
        CheckinResultVO result = service.doCheckin(7L, req(104.065735, 30.657008, 30.0));

        assertEquals(1L, result.getFenceId());
        assertEquals(0.0, result.getDistanceM(), 1e-6);

        ArgumentCaptor<CheckinRecord> captor = ArgumentCaptor.forClass(CheckinRecord.class);
        verify(recordMapper).insert(captor.capture());
        CheckinRecord saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals(1L, saved.getFenceId());
        assertEquals("H5", saved.getSource());
        assertEquals(0, saved.getSuspicious());
        assertEquals(0.0, saved.getDistanceM(), 1e-6);
    }

    @Test
    void outsideAllFencesRejectedWithNearestHint() {
        when(fenceMapper.selectList(any())).thenReturn(List.of(fence(1, "总部", 104.065735, 30.657008, 200)));
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.doCheckin(7L, req(104.10, 30.70, 30.0)));
        assertTrue(e.getMessage().contains("不在任何打卡围栏内"));
        assertTrue(e.getMessage().contains("总部"));
    }

    @Test
    void noEnabledFencesRejected() {
        when(fenceMapper.selectList(any())).thenReturn(List.of());
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.doCheckin(7L, req(104.0, 30.6, null)));
        assertTrue(e.getMessage().contains("不在任何打卡围栏内"));
    }

    @Test
    void overlappingFencesPicksNearest() {
        GeoFence near = fence(2, "近围栏", 104.0660, 30.657008, 500);
        GeoFence far = fence(1, "远围栏", 104.0500, 30.657008, 5000);
        when(fenceMapper.selectList(any())).thenReturn(List.of(far, near));
        CheckinResultVO result = service.doCheckin(7L, req(104.0660, 30.657008, 20.0));
        assertEquals(2L, result.getFenceId());
    }

    @Test
    void poorAccuracyMarksSuspiciousButStillPasses() {
        when(fenceMapper.selectList(any())).thenReturn(List.of(fence(1, "总部", 104.065735, 30.657008, 200)));
        service.doCheckin(7L, req(104.065735, 30.657008, 500.0));
        ArgumentCaptor<CheckinRecord> captor = ArgumentCaptor.forClass(CheckinRecord.class);
        verify(recordMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getSuspicious());
    }

    @Test
    void goodAccuracyNotSuspicious() {
        when(fenceMapper.selectList(any())).thenReturn(List.of(fence(1, "总部", 104.065735, 30.657008, 200)));
        service.doCheckin(7L, req(104.065735, 30.657008, 50.0));
        ArgumentCaptor<CheckinRecord> captor = ArgumentCaptor.forClass(CheckinRecord.class);
        verify(recordMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getSuspicious());
    }
}

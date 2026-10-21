package com.geofence.checkin.biz.rule.service;

import com.geofence.checkin.biz.rule.dto.RuleSaveReq;
import com.geofence.checkin.biz.rule.entity.CheckinRuleScope;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleMapper;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleScopeMapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.geo.service.GeoFenceService;
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
 * 规则管理单测：跨天班次拒绝、非全员必须绑定对象、围栏必须存在、scope 落库。
 */
class RuleServiceTest {

    private final CheckinRuleMapper ruleMapper = mock(CheckinRuleMapper.class);
    private final CheckinRuleScopeMapper scopeMapper = mock(CheckinRuleScopeMapper.class);
    private final GeoFenceService geoFenceService = mock(GeoFenceService.class);
    private final RuleService service = new RuleService(ruleMapper, scopeMapper, geoFenceService);

    private RuleSaveReq req(String bindType, List<Long> userIds) {
        RuleSaveReq r = new RuleSaveReq();
        r.setName("标准班");
        r.setFenceId(1L);
        r.setWorkStartSec(9 * 3600);
        r.setWorkEndSec(18 * 3600);
        r.setBindType(bindType);
        r.setUserIds(userIds);
        return r;
    }

    private void fenceExists() {
        when(geoFenceService.get(1L)).thenReturn(new com.geofence.checkin.core.geo.entity.GeoFence());
    }

    @Test
    void crossDayShiftRejected() {
        fenceExists();
        RuleSaveReq r = req("ALL", null);
        r.setWorkStartSec(22 * 3600);
        r.setWorkEndSec(6 * 3600);
        BusinessException e = assertThrows(BusinessException.class, () -> service.create(r));
        assertTrue(e.getMessage().contains("跨天"));
    }

    @Test
    void nonAllRuleWithoutScopeRejected() {
        fenceExists();
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.create(req("USER", List.of())));
        assertTrue(e.getMessage().contains("绑定对象"));
    }

    @Test
    void missingFenceRejected() {
        when(geoFenceService.get(99L)).thenThrow(new BusinessException("围栏不存在"));
        RuleSaveReq r = req("ALL", null);
        r.setFenceId(99L);
        BusinessException e = assertThrows(BusinessException.class, () -> service.create(r));
        assertTrue(e.getMessage().contains("围栏不存在"));
    }

    @Test
    void createPersistsScopesForEachBinding() {
        fenceExists();
        when(ruleMapper.insert(any(com.geofence.checkin.biz.rule.entity.CheckinRule.class))).thenAnswer(inv -> {
            com.geofence.checkin.biz.rule.entity.CheckinRule rule = inv.getArgument(0);
            rule.setId(42L);
            return 1;
        });
        RuleSaveReq r = new RuleSaveReq();
        r.setName("复杂班");
        r.setFenceId(1L);
        r.setWorkStartSec(9 * 3600);
        r.setWorkEndSec(18 * 3600);
        r.setBindType("USER");
        r.setUserIds(List.of(7L, 8L));
        Long id = service.create(r);
        assertEquals(42L, id);

        ArgumentCaptor<CheckinRuleScope> captor = ArgumentCaptor.forClass(CheckinRuleScope.class);
        verify(scopeMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        List<CheckinRuleScope> scopes = captor.getAllValues();
        assertEquals(42L, scopes.get(0).getRuleId());
        assertEquals(7L, scopes.get(0).getUserId());
        assertEquals(8L, scopes.get(1).getUserId());
    }

    @Test
    void defaultsAppliedOnCreate() {
        fenceExists();
        when(ruleMapper.insert(any(com.geofence.checkin.biz.rule.entity.CheckinRule.class))).thenAnswer(inv -> {
            com.geofence.checkin.biz.rule.entity.CheckinRule rule = inv.getArgument(0);
            rule.setId(1L);
            return 1;
        });
        service.create(req(null, null));
        ArgumentCaptor<com.geofence.checkin.biz.rule.entity.CheckinRule> captor =
                ArgumentCaptor.forClass(com.geofence.checkin.biz.rule.entity.CheckinRule.class);
        verify(ruleMapper).insert(captor.capture());
        com.geofence.checkin.biz.rule.entity.CheckinRule saved = captor.getValue();
        assertEquals("ALL", saved.getBindType());
        assertEquals(127, saved.getEffectiveDays());
        assertEquals(0, saved.getLateToleranceSec());
        assertEquals(1, saved.getStatus());
        assertEquals(0, saved.getDeleted());
    }
}

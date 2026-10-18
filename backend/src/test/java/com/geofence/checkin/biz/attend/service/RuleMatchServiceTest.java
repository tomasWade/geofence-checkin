package com.geofence.checkin.biz.attend.service;

import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.biz.rule.entity.CheckinRuleScope;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleMapper;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleScopeMapper;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.entity.SysUserRole;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import com.geofence.checkin.core.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 规则匹配单测：USER > GROUP > ROLE > ALL 优先级、生效日 bitmask、停用规则忽略。
 */
class RuleMatchServiceTest {

    private final CheckinRuleMapper ruleMapper = mock(CheckinRuleMapper.class);
    private final CheckinRuleScopeMapper scopeMapper = mock(CheckinRuleScopeMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final SysGroupMemberMapper groupMemberMapper = mock(SysGroupMemberMapper.class);
    private final RuleMatchService service = new RuleMatchService(ruleMapper, scopeMapper, userRoleMapper, groupMemberMapper);

    private final LocalDate friday = LocalDate.of(2026, 10, 16);
    private final LocalDate sunday = LocalDate.of(2026, 10, 18);

    private CheckinRule rule(long id, String name, String bindType, int effectiveDays) {
        CheckinRule r = new CheckinRule();
        r.setId(id);
        r.setName(name);
        r.setBindType(bindType);
        r.setEffectiveDays(effectiveDays);
        r.setStatus(1);
        return r;
    }

    private CheckinRuleScope scope(long ruleId, Long roleId, Long groupId, Long userId) {
        CheckinRuleScope s = new CheckinRuleScope();
        s.setRuleId(ruleId);
        s.setRoleId(roleId);
        s.setGroupId(groupId);
        s.setUserId(userId);
        return s;
    }

    private void me(Long roleId, Long groupId, long userId) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        when(userRoleMapper.selectList(any())).thenReturn(roleId == null ? List.of() : List.of(ur));
        SysGroupMember gm = new SysGroupMember();
        gm.setUserId(userId);
        gm.setGroupId(groupId);
        when(groupMemberMapper.selectList(any())).thenReturn(groupId == null ? List.of() : List.of(gm));
    }

    @Test
    void matchesAllRuleWhenNothingMoreSpecific() {
        CheckinRule all = rule(1, "全员", "ALL", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(all));
        me(null, null, 7L);
        assertEquals(1L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void userBindingBeatsGroupRoleAndAll() {
        CheckinRule all = rule(1, "全员", "ALL", 127);
        CheckinRule grp = rule(2, "组班", "GROUP", 127);
        CheckinRule usr = rule(3, "专班", "USER", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(all, grp, usr));
        me(1L, 10L, 7L);
        when(scopeMapper.selectList(any())).thenReturn(
                List.of(scope(2, null, 10L, null), scope(3, null, null, 7L)));
        assertEquals(3L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void groupBindingBeatsRoleAndAll() {
        CheckinRule all = rule(1, "全员", "ALL", 127);
        CheckinRule grp = rule(2, "组班", "GROUP", 127);
        CheckinRule rol = rule(3, "角色班", "ROLE", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(all, grp, rol));
        me(1L, 10L, 7L);
        when(scopeMapper.selectList(any())).thenReturn(
                List.of(scope(2, null, 10L, null), scope(3, 1L, null, null)));
        assertEquals(2L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void roleBindingBeatsAll() {
        CheckinRule all = rule(1, "全员", "ALL", 127);
        CheckinRule rol = rule(2, "角色班", "ROLE", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(all, rol));
        me(1L, null, 7L);
        when(scopeMapper.selectList(any())).thenReturn(List.of(scope(2, 1L, null, null)));
        assertEquals(2L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void samePriorityPicksSmallestId() {
        CheckinRule g1 = rule(5, "组班A", "GROUP", 127);
        CheckinRule g2 = rule(4, "组班B", "GROUP", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(g1, g2));
        me(null, 10L, 7L);
        when(scopeMapper.selectList(any())).thenReturn(
                List.of(scope(5, null, 10L, null), scope(4, null, 10L, null)));
        assertEquals(4L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void weekdayNotEffectiveReturnsEmpty() {
        CheckinRule all = rule(1, "工作日班", "ALL", 0b0011111);
        when(ruleMapper.selectList(any())).thenReturn(List.of(all));
        me(null, null, 7L);
        assertTrue(service.match(7L, sunday).isEmpty());
    }

    @Test
    void noRulesReturnsEmpty() {
        when(ruleMapper.selectList(any())).thenReturn(List.of());
        me(null, null, 7L);
        assertTrue(service.match(7L, friday).isEmpty());
    }

    @Test
    void scopeNotMatchingFallsThrough() {
        CheckinRule usr = rule(1, "专班", "USER", 127);
        CheckinRule all = rule(2, "全员", "ALL", 127);
        when(ruleMapper.selectList(any())).thenReturn(List.of(usr, all));
        me(null, null, 7L);
        when(scopeMapper.selectList(any())).thenReturn(List.of(scope(1, null, null, 99L)));
        assertEquals(2L, service.match(7L, friday).orElseThrow().getId());
    }

    @Test
    void mondayIsBitZero() {
        CheckinRule monOnly = rule(1, "周一班", "ALL", 0b0000001);
        when(ruleMapper.selectList(any())).thenReturn(List.of(monOnly));
        me(null, null, 7L);
        assertEquals(1L, service.match(7L, LocalDate.of(2026, 10, 19)).orElseThrow().getId());
        assertTrue(service.match(7L, LocalDate.of(2026, 10, 20)).isEmpty());
    }
}

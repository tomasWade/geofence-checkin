package com.geofence.checkin.core.system.service;

import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.system.entity.SysGroup;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.entity.SysRole;
import com.geofence.checkin.core.system.mapper.SysGroupMapper;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 数据范围单测：SELF/GROUP/ALL 三档、组长不在成员表语义、越权 checkAccess。
 */
class DataScopeServiceTest {

    private final RoleService roleService = mock(RoleService.class);
    private final SysGroupMemberMapper groupMemberMapper = mock(SysGroupMemberMapper.class);
    private final SysGroupMapper groupMapper = mock(SysGroupMapper.class);
    private final DataScopeService service = new DataScopeService(roleService, groupMemberMapper, groupMapper);

    @AfterEach
    void clean() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, List.of()));
    }

    private SysRole role(String scope) {
        SysRole r = new SysRole();
        r.setId(1L);
        r.setRoleCode("X");
        r.setDataScope(scope);
        return r;
    }

    @Test
    void selfScopeSeesOnlyOneself() {
        loginAs(7L);
        when(roleService.findRolesByUserId(7L)).thenReturn(List.of(role("SELF")));
        Set<Long> visible = service.visibleUserIds();
        assertNotNull(visible);
        assertEquals(Set.of(7L), visible);
    }

    @Test
    void allScopeReturnsNullMeaningNoFilter() {
        loginAs(1L);
        when(roleService.findRolesByUserId(1L)).thenReturn(List.of(role("ALL")));
        assertNull(service.visibleUserIds());
    }

    @Test
    void groupScopeUnionsAllMyGroupsMembers() {
        loginAs(2L);
        when(roleService.findRolesByUserId(2L)).thenReturn(List.of(role("GROUP")));
        when(groupMemberMapper.selectList(any())).thenReturn(
                List.of(member(10L, 2L), member(10L, 3L), member(11L, 2L)),
                List.of(member(10L, 2L), member(10L, 3L), member(11L, 5L)));
        when(groupMapper.selectList(any())).thenReturn(List.of());
        Set<Long> visible = service.visibleUserIds();
        assertEquals(Set.of(2L, 3L, 5L), visible);
    }

    @Test
    void groupScopeIncludesGroupsWhereIAmManagerButNotMember() {
        loginAs(9L);
        when(roleService.findRolesByUserId(9L)).thenReturn(List.of(role("GROUP")));
        SysGroup managed = new SysGroup();
        managed.setId(20L);
        managed.setManagerUserId(9L);
        when(groupMemberMapper.selectList(any())).thenReturn(
                List.of(),
                List.of(member(20L, 30L), member(20L, 31L)));
        when(groupMapper.selectList(any())).thenReturn(List.of(managed));
        Set<Long> visible = service.visibleUserIds();
        assertEquals(Set.of(9L, 30L, 31L), visible);
    }

    @Test
    void groupScopeWithNoGroupsFallsBackToSelf() {
        loginAs(4L);
        when(roleService.findRolesByUserId(4L)).thenReturn(List.of(role("GROUP")));
        when(groupMemberMapper.selectList(any())).thenReturn(List.of());
        when(groupMapper.selectList(any())).thenReturn(List.of());
        assertEquals(Set.of(4L), service.visibleUserIds());
    }

    @Test
    void checkAccessThrowsForOutOfWorkScopeUser() {
        loginAs(7L);
        when(roleService.findRolesByUserId(7L)).thenReturn(List.of(role("SELF")));
        BusinessException e = assertThrows(BusinessException.class, () -> service.checkAccess(8L));
        assertEquals(403, e.getCode());
    }

    @Test
    void checkAccessPassesForSelf() {
        loginAs(7L);
        when(roleService.findRolesByUserId(7L)).thenReturn(List.of(role("SELF")));
        service.checkAccess(7L);
        assertTrue(true);
    }

    @Test
    void higherScopeWinsWhenMultipleRoles() {
        loginAs(5L);
        when(roleService.findRolesByUserId(5L)).thenReturn(List.of(role("SELF"), role("ALL")));
        assertNull(service.visibleUserIds());
        Mockito.verifyNoInteractions(groupMemberMapper);
    }

    private SysGroupMember member(long groupId, long userId) {
        SysGroupMember m = new SysGroupMember();
        m.setGroupId(groupId);
        m.setUserId(userId);
        return m;
    }
}

package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.system.entity.SysGroup;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.mapper.SysGroupMapper;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据范围核心机制：当前用户可见的用户 ID 集合（SELF=本人 / GROUP=所在组与所管组成员并集 / ALL=不过滤）；列表注入与单点 checkAccess 双闸。
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_GROUP = "GROUP";
    public static final String SCOPE_SELF = "SELF";

    private final RoleService roleService;
    private final SysGroupMemberMapper groupMemberMapper;
    private final SysGroupMapper groupMapper;

    public String currentScope() {
        List<com.geofence.checkin.core.system.entity.SysRole> roles =
                roleService.findRolesByUserId(CurrentUser.requireUserId());
        if (roles.stream().anyMatch(r -> SCOPE_ALL.equals(r.getDataScope()))) {
            return SCOPE_ALL;
        }
        if (roles.stream().anyMatch(r -> SCOPE_GROUP.equals(r.getDataScope()))) {
            return SCOPE_GROUP;
        }
        return SCOPE_SELF;
    }

    public Set<Long> visibleUserIds() {
        String scope = currentScope();
        return visibleUserIds(scope, CurrentUser.requireUserId());
    }

    public Set<Long> visibleUserIds(String scope, Long userId) {
        return switch (scope) {
            case SCOPE_ALL -> null;
            case SCOPE_SELF -> Set.of(userId);
            case SCOPE_GROUP -> groupVisibleUserIds(userId);
            default -> Set.of(userId);
        };
    }

    public boolean canSee(Long targetUserId) {
        String scope = currentScope();
        if (SCOPE_ALL.equals(scope)) {
            return true;
        }
        if (SCOPE_SELF.equals(scope)) {
            return CurrentUser.requireUserId().equals(targetUserId);
        }
        return groupVisibleUserIds(CurrentUser.requireUserId()).contains(targetUserId);
    }

    public void checkAccess(Long targetUserId) {
        if (!canSee(targetUserId)) {
            throw new BusinessException(403, "无权访问该用户数据");
        }
    }

    private Set<Long> groupVisibleUserIds(Long userId) {
        Set<Long> groupIds = new HashSet<>();
        List<SysGroupMember> myGroups = groupMemberMapper.selectList(
                new LambdaQueryWrapper<SysGroupMember>().eq(SysGroupMember::getUserId, userId));
        myGroups.forEach(m -> groupIds.add(m.getGroupId()));
        groupMapper.selectList(new LambdaQueryWrapper<SysGroup>()
                        .eq(SysGroup::getManagerUserId, userId))
                .forEach(g -> groupIds.add(g.getId()));
        if (groupIds.isEmpty()) {
            return Set.of(userId);
        }
        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<SysGroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<SysGroupMember>().in(SysGroupMember::getGroupId, groupIds));
        members.forEach(m -> userIds.add(m.getUserId()));
        return Collections.unmodifiableSet(userIds);
    }
}

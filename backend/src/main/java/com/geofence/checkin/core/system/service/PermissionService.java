package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.core.system.entity.SysPermission;
import com.geofence.checkin.core.system.entity.SysRolePermission;
import com.geofence.checkin.core.system.entity.SysUserRole;
import com.geofence.checkin.core.system.mapper.SysPermissionMapper;
import com.geofence.checkin.core.system.mapper.SysRolePermissionMapper;
import com.geofence.checkin.core.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限查询：userId -> 角色 -> 权限码集合（每请求由过滤器调用）。
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    public Set<String> findPermCodesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds));
        if (rps.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> permIds = rps.stream().map(SysRolePermission::getPermissionId).distinct().toList();
        return permissionMapper.selectBatchIds(permIds).stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toSet());
    }
}

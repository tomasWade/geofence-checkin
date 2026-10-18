package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.core.system.entity.SysRole;
import com.geofence.checkin.core.system.entity.SysUserRole;
import com.geofence.checkin.core.system.mapper.SysRoleMapper;
import com.geofence.checkin.core.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 角色查询：按用户查角色列表/角色码，全量角色列表。
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    public List<SysRole> findRolesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return roleMapper.selectBatchIds(roleIds);
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        return findRolesByUserId(userId).stream().map(SysRole::getRoleCode).toList();
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(null);
    }
}

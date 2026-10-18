package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.system.dto.UserCreateReq;
import com.geofence.checkin.core.system.dto.UserUpdateReq;
import com.geofence.checkin.core.system.dto.UserVO;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.entity.SysRole;
import com.geofence.checkin.core.system.entity.SysUser;
import com.geofence.checkin.core.system.entity.SysUserRole;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import com.geofence.checkin.core.system.mapper.SysUserMapper;
import com.geofence.checkin.core.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理：分页/新增（用户名唯一竞态兜底）/编辑/逻辑删（清角色与分组成员，禁止删自己）/分配角色。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysGroupMemberMapper groupMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public Page<UserVO> page(long page, long size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword))
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(Page.of(page, size), wrapper);
        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    public UserVO get(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Transactional
    public Long create(UserCreateReq req) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName());
        user.setMobile(req.getMobile());
        user.setStatus(1);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名已存在");
        }
        if (req.getRoleId() != null) {
            assignRole(user.getId(), List.of(req.getRoleId()));
        }
        return user.getId();
    }

    @Transactional
    public void update(UserUpdateReq req) {
        SysUser user = userMapper.selectById(req.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getMobile() != null) {
            user.setMobile(req.getMobile());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        Long currentId = com.geofence.checkin.core.auth.security.CurrentUser.userId();
        if (currentId != null && currentId.equals(id)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        groupMemberMapper.delete(new LambdaQueryWrapper<SysGroupMember>()
                .eq(SysGroupMember::getUserId, id));
    }

    @Transactional
    public void assignRole(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysRole role = roleService.listAll().stream()
                    .filter(r -> r.getId().equals(roleId)).findFirst()
                    .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
    }

    public Map<Long, String> nameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername()));
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        List<SysRole> roles = roleService.findRolesByUserId(user.getId());
        vo.setRoleIds(roles.stream().map(SysRole::getId).toList());
        vo.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
        return vo;
    }
}

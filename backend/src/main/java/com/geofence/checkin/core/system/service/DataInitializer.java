package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.core.system.entity.SysGroup;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.entity.SysRole;
import com.geofence.checkin.core.system.entity.SysUser;
import com.geofence.checkin.core.system.entity.SysUserRole;
import com.geofence.checkin.core.system.mapper.SysGroupMapper;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import com.geofence.checkin.core.system.mapper.SysRoleMapper;
import com.geofence.checkin.core.system.mapper.SysUserMapper;
import com.geofence.checkin.core.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始账号引导（app.init-enabled=true 且用户表为空时）：创建 admin/manager/employee 与示范组，按 roleCode 关联角色。
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysGroupMapper groupMapper;
    private final SysGroupMemberMapper groupMemberMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-enabled:false}")
    private boolean initEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!initEnabled) {
            return;
        }
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        Long adminId = createUser("admin", "admin123", "管理员", "ADMIN");
        Long managerId = createUser("manager", "manager123", "王经理", "MANAGER");
        Long employeeId = createUser("employee", "employee123", "小李", "EMPLOYEE");

        SysGroup group = new SysGroup();
        group.setName("示范组");
        group.setManagerUserId(managerId);
        groupMapper.insert(group);

        insertMember(group.getId(), managerId);
        insertMember(group.getId(), employeeId);

        log.info("初始账号已创建: admin/admin123, manager/manager123, employee/employee123 (示范组)");
    }

    private Long createUser(String username, String password, String realName, String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            throw new IllegalStateException("种子角色缺失: " + roleCode);
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);
        return user.getId();
    }

    private void insertMember(Long groupId, Long userId) {
        SysGroupMember m = new SysGroupMember();
        m.setGroupId(groupId);
        m.setUserId(userId);
        groupMemberMapper.insert(m);
    }
}

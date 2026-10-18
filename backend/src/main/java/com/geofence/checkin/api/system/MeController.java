package com.geofence.checkin.api.system;

import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.auth.dto.LoginResp;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.system.entity.SysRole;
import com.geofence.checkin.core.system.service.PermissionService;
import com.geofence.checkin.core.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户接口：/system/me（登录后首拉权限码）与角色列表。
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class MeController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    @GetMapping("/me")
    public ApiResult<LoginResp> me() {
        Long userId = CurrentUser.requireUserId();
        return ApiResult.ok(LoginResp.builder()
                .userId(userId)
                .roles(roleService.findRoleCodesByUserId(userId))
                .perms(permissionService.findPermCodesByUserId(userId))
                .build());
    }

    @GetMapping("/roles")
    @PreAuthorize("@ss.hasPermi('sys:user:list')")
    public ApiResult<List<SysRole>> roles() {
        return ApiResult.ok(roleService.listAll());
    }
}

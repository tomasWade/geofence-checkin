package com.geofence.checkin.api.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.system.audit.Audit;
import com.geofence.checkin.core.system.dto.RoleAssignReq;
import com.geofence.checkin.core.system.dto.UserCreateReq;
import com.geofence.checkin.core.system.dto.UserUpdateReq;
import com.geofence.checkin.core.system.dto.UserVO;
import com.geofence.checkin.core.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口：分页/增删改/分配角色。
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('sys:user:list')")
    public ApiResult<Page<UserVO>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size,
                                        @RequestParam(required = false) String keyword) {
        return ApiResult.ok(userService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('sys:user:list')")
    public ApiResult<UserVO> get(@PathVariable Long id) {
        return ApiResult.ok(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('sys:user:add')")
    @Audit(action = "user:create")
    public ApiResult<Long> create(@Valid @RequestBody UserCreateReq req) {
        return ApiResult.ok(userService.create(req));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('sys:user:edit')")
    @Audit(action = "user:update")
    public ApiResult<Void> update(@Valid @RequestBody UserUpdateReq req) {
        userService.update(req);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('sys:user:delete')")
    @Audit(action = "user:delete")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    @PutMapping("/roles")
    @PreAuthorize("@ss.hasPermi('sys:user:assignRole')")
    @Audit(action = "user:assignRole")
    public ApiResult<Void> assignRoles(@Valid @RequestBody RoleAssignReq req) {
        userService.assignRole(req.getUserId(), req.getRoleIds());
        return ApiResult.ok();
    }
}

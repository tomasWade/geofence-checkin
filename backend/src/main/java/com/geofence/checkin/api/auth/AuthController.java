package com.geofence.checkin.api.auth;

import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.common.util.IpUtil;
import com.geofence.checkin.core.auth.dto.LoginReq;
import com.geofence.checkin.core.auth.dto.LoginResp;
import com.geofence.checkin.core.auth.dto.RefreshReq;
import com.geofence.checkin.core.auth.service.AuthService;
import com.geofence.checkin.core.system.audit.Audit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录（含审计）/刷新/登出。
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Audit(action = "auth:login")
    public ApiResult<LoginResp> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        return ApiResult.ok(authService.login(req.getUsername(), req.getPassword(), IpUtil.clientIp(request)));
    }

    @PostMapping("/refresh")
    public ApiResult<LoginResp> refresh(@Valid @RequestBody RefreshReq req) {
        return ApiResult.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestBody(required = false) RefreshReq req) {
        authService.logout(req == null ? null : req.getRefreshToken());
        return ApiResult.ok();
    }
}

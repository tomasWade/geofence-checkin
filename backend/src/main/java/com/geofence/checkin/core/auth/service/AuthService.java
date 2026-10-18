package com.geofence.checkin.core.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.auth.dto.LoginResp;
import com.geofence.checkin.core.auth.jwt.JwtService;
import com.geofence.checkin.core.system.entity.SysUser;
import com.geofence.checkin.core.system.mapper.SysUserMapper;
import com.geofence.checkin.core.system.service.PermissionService;
import com.geofence.checkin.core.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务：登录（失败限速、双维度计数锁定）、refresh token 原子轮换、登出吊销。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final StringRedisTemplate redis;

    public LoginResp login(String username, String password, String clientIp) {
        String perIpKey = "chk:login:fail:" + username + ":" + clientIp;
        String perUserKey = "chk:login:failu:" + username;
        try {
            String ipFails = redis.opsForValue().get(perIpKey);
            if (ipFails != null && Integer.parseInt(ipFails) >= 5) {
                throw new BusinessException(429, "失败次数过多，请 10 分钟后重试");
            }
            String userFails = redis.opsForValue().get(perUserKey);
            if (userFails != null && Integer.parseInt(userFails) >= 20) {
                throw new BusinessException(429, "该账号失败次数过多，已临时锁定");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(503, "登录服务暂不可用，请稍后重试");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            try {
                redis.opsForValue().increment(perIpKey);
                redis.expire(perIpKey, java.time.Duration.ofMinutes(10));
                redis.opsForValue().increment(perUserKey);
                redis.expire(perUserKey, java.time.Duration.ofMinutes(30));
            } catch (Exception ignored) {
            }
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已禁用");
        }
        try {
            redis.delete(perIpKey);
            redis.delete(perUserKey);
        } catch (Exception ignored) {
        }
        return buildLoginResp(user);
    }

    public LoginResp refresh(String refreshToken) {
        Long userId = refreshTokenStore.findUserIdAndRevoke(refreshToken);
        if (userId == null) {
            throw new BusinessException(401, "refreshToken 无效或已过期");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "用户不存在或已禁用");
        }
        return buildLoginResp(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.revoke(refreshToken);
        }
    }

    private LoginResp buildLoginResp(SysUser user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refresh = UUID.randomUUID().toString().replace("-", "");
        refreshTokenStore.save(refresh, user.getId());
        return LoginResp.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roleService.findRoleCodesByUserId(user.getId()))
                .perms(permissionService.findPermCodesByUserId(user.getId()))
                .build();
    }
}

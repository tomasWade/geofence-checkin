package com.geofence.checkin.core.auth.service;

import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * refresh token 的 Redis 存储：保存（带 TTL）、原子取出即作废（防重放）、吊销；Redis 不可用时快速失败为 503。
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String PREFIX = "chk:rt:";

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    public void save(String refreshToken, Long userId) {
        try {
            redis.opsForValue().set(PREFIX + refreshToken, String.valueOf(userId),
                    Duration.ofDays(jwtProperties.getRefreshTokenTtlDays()));
        } catch (Exception e) {
            throw new BusinessException(503, "登录服务暂不可用，请稍后重试");
        }
    }

    public Long findUserIdAndRevoke(String refreshToken) {
        try {
            String v = redis.opsForValue().getAndDelete(PREFIX + refreshToken);
            return v == null ? null : Long.valueOf(v);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(503, "登录服务暂不可用，请稍后重试");
        }
    }

    public void revoke(String refreshToken) {
        try {
            redis.delete(PREFIX + refreshToken);
        } catch (Exception ignored) {
        }
    }
}

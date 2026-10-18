package com.geofence.checkin.core.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：从 SecurityContext 取 JWT 中的 userId。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long requireUserId() {
        Long id = userId();
        if (id == null) {
            throw new IllegalStateException("未认证请求");
        }
        return id;
    }
}

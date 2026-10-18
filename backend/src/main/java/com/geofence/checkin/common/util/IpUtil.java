package com.geofence.checkin.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 提取：取 X-Forwarded-For 最后一段（自有 nginx 追加的真实 IP），无代理头时取远端地址。
 */
public final class IpUtil {

    private IpUtil() {
    }

    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] parts = xff.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}

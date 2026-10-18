package com.geofence.checkin.core.auth.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项：密钥、access token 有效期（分钟）、refresh token 有效期（天）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private int accessTokenTtlMinutes = 120;
    private int refreshTokenTtlDays = 14;

    @jakarta.annotation.PostConstruct
    void warnOnDefaultSecret() {
        if (secret == null || secret.contains("CHANGE-IN-PROD")) {
            org.slf4j.LoggerFactory.getLogger(JwtProperties.class)
                    .warn("JWT secret 未配置或仍为开发默认值，生产环境必须通过环境变量注入强随机 secret！");
        }
    }
}

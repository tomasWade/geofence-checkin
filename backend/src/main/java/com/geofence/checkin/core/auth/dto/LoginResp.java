package com.geofence.checkin.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 登录/刷新响应体：双 token + 用户信息 + 角色与权限码集合。
 */
@Data
@Builder
@AllArgsConstructor
public class LoginResp {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private Set<String> perms;
}

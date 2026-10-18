package com.geofence.checkin.core.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 权限码校验器（Bean 名 ss），供 @PreAuthorize("@ss.hasPermi('xxx')") 使用；代码只认权限码不认角色名。
 */
@Component("ss")
public class PermissionChecker {

    public boolean hasPermi(String permCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> Objects.equals(a, permCode));
    }

    public boolean hasAnyPermi(String... permCodes) {
        for (String code : permCodes) {
            if (hasPermi(code)) {
                return true;
            }
        }
        return false;
    }
}

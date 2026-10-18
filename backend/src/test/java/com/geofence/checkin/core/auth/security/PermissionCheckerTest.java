package com.geofence.checkin.core.auth.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 权限码校验器单测：hasPermi / hasAnyPermi 与未认证场景。
 */
class PermissionCheckerTest {

    private final PermissionChecker checker = new PermissionChecker();

    @AfterEach
    void clean() {
        SecurityContextHolder.clearContext();
    }

    private void login(String... perms) {
        List<SimpleGrantedAuthority> auths = List.of(perms).stream()
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, auths));
    }

    @Test
    void hasPermiReturnsTrueWhenAuthorityMatches() {
        login("fence:list", "fence:add");
        assertTrue(checker.hasPermi("fence:list"));
        assertTrue(checker.hasPermi("fence:add"));
    }

    @Test
    void hasPermiReturnsFalseWhenAuthorityMissing() {
        login("fence:list");
        assertFalse(checker.hasPermi("sys:user:list"));
    }

    @Test
    void hasPermiReturnsFalseWhenNotAuthenticated() {
        assertFalse(checker.hasPermi("fence:list"));
    }

    @Test
    void hasAnyPermiMatchesOneOfMany() {
        login("record:list");
        assertTrue(checker.hasAnyPermi("sys:user:list", "record:list", "report:daily"));
    }

    @Test
    void hasAnyPermiReturnsFalseWhenNoneMatch() {
        login("record:list");
        assertFalse(checker.hasAnyPermi("a", "b"));
    }
}

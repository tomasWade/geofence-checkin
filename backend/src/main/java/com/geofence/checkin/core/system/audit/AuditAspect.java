package com.geofence.checkin.core.system.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geofence.checkin.common.util.IpUtil;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.system.entity.SysAuditLog;
import com.geofence.checkin.core.system.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计切面：成功与失败（action:fail）均落审计；REQUIRES_NEW 独立事务保证失败记录不被业务回滚；密码字段脱敏，Servlet 对象以类名替代。
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate auditTx;

    public AuditAspect(SysAuditLogMapper auditLogMapper, ObjectMapper objectMapper,
                       PlatformTransactionManager txManager) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
        this.auditTx = new TransactionTemplate(txManager);
        this.auditTx.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @AfterReturning("@annotation(audit)")
    public void record(JoinPoint jp, Audit audit) {
        save(jp, audit, null);
    }

    @AfterThrowing(value = "@annotation(audit)", throwing = "ex")
    public void recordFailure(JoinPoint jp, Audit audit, Exception ex) {
        save(jp, audit, ex);
    }

    private void save(JoinPoint jp, Audit audit, Exception ex) {
        try {
            SysAuditLog entry = new SysAuditLog();
            entry.setUserId(CurrentUser.userId());
            entry.setAction(ex == null ? audit.action() : audit.action() + ":fail");
            entry.setResource(jp.getSignature().toShortString());
            String detail = objectMapper.writeValueAsString(sanitize(jp.getArgs()));
            if (ex != null) {
                detail = detail + " | error=" + ex.getMessage();
            }
            entry.setDetailJson(detail);
            entry.setIp(currentIp());
            auditTx.executeWithoutResult(s -> auditLogMapper.insert(entry));
        } catch (Exception e) {
            log.warn("audit log failed: {}", e.getMessage());
        }
    }

    private Object[] sanitize(Object[] args) {
        Object[] copy = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            copy[i] = sanitizeOne(args[i]);
        }
        return copy;
    }

    private Object sanitizeOne(Object arg) {
        if (arg instanceof java.util.Map<?, ?> m) {
            return sanitizeMap(m);
        }
        if (arg instanceof jakarta.servlet.ServletRequest
                || arg instanceof jakarta.servlet.ServletResponse
                || arg instanceof java.io.InputStream
                || arg instanceof org.springframework.web.multipart.MultipartFile) {
            return arg.getClass().getSimpleName();
        }
        return arg;
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> sanitizeMap(java.util.Map<?, ?> m) {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        m.forEach((k, v) -> {
            String key = String.valueOf(k);
            if (key.toLowerCase().contains("password")) {
                out.put(key, "***");
            } else {
                out.put(key, v);
            }
        });
        return out;
    }

    private String currentIp() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return IpUtil.clientIp(request);
    }
}

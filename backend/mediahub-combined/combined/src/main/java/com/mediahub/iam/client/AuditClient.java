package com.mediahub.iam.client;

import com.mediahub.auditlog.entity.AuditEvent;
import com.mediahub.auditlog.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Records audit events for IAM actions (user suspend/activate, role/permission changes).
 * IAM and the audit log live in the same module/JVM, so this calls AuditEventService
 * directly rather than going over HTTP like the other services' AuditClients.
 */
@Service
public class AuditClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditClient.class);

    @Autowired
    private AuditEventService auditEventService;

    public void log(String eventType, Long performedBy, String performedByRole,
                     String targetEntityType, String targetEntityId,
                     String description, AuditEvent.Severity severity) {
        try {
            auditEventService.logEvent(
                eventType,
                AuditEvent.ModuleSource.IAM,
                performedBy,
                performedByRole,
                targetEntityType,
                targetEntityId,
                null,
                null,
                resolveClientIp(),
                severity,
                AuditEvent.EventStatus.SUCCESS,
                description);
        } catch (Exception e) {
            logger.error("Failed to record audit event type={} entity={}/{}: {}",
                eventType, targetEntityType, targetEntityId, e.getMessage());
        }
    }

    // The gateway is the only thing that ever calls this service directly, so without
    // X-Forwarded-For every audit event would just record the gateway's own address instead
    // of the real caller. Falls back to the direct remote address when the header is absent
    // (e.g. a service-to-service call made outside the gateway).
    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
            return req.getRemoteAddr();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}

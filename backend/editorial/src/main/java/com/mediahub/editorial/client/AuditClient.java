package com.mediahub.editorial.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Fire-and-forget client that records an audit event with the IAM/audit service.
 * Never blocks the caller and never fails the primary action if the audit
 * service is slow or unreachable.
 */
@Service
public class AuditClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${audit.service.url:http://localhost:8091}")
    private String auditServiceUrl;

    public void log(String eventType, String moduleSource, Long performedBy, String performedByRole,
                     String targetEntityType, String targetEntityId, String description, String severity) {
        Map<String, Object> body = new HashMap<>();
        body.put("eventType", eventType);
        body.put("moduleSource", moduleSource);
        body.put("performedBy", performedBy);
        body.put("performedByRole", performedByRole);
        body.put("targetEntityType", targetEntityType);
        body.put("targetEntityId", targetEntityId);
        body.put("description", description);
        body.put("severity", severity);
        body.put("ipAddress", resolveClientIp());

        webClient.post()
                .uri(auditServiceUrl + "/mediaHub/auditlog/events/logEvent/v1.0")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                    response -> logger.debug("Audit event recorded: {}", eventType),
                    error -> logger.error("Failed to record audit event type={} entity={}/{}: {}",
                        eventType, targetEntityType, targetEntityId, error.getMessage()));
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
            return normalizeLoopback(req.getRemoteAddr());
        } catch (IllegalStateException e) {
            return null;
        }
    }

    // Only reached when this service is called directly, bypassing the gateway (e.g. a local
    // test) — the gateway already normalizes X-Forwarded-For for the normal request path. Java
    // reports the IPv6 loopback as "0:0:0:0:0:0:0:1" and the IPv4 loopback as "127.0.0.1" for
    // what is the same machine, so collapse both to one canonical string here too.
    private String normalizeLoopback(String ip) {
        return ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) ? "127.0.0.1" : ip;
    }
}

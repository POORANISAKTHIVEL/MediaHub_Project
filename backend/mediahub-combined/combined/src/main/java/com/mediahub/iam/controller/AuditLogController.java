package com.mediahub.iam.controller;

import com.mediahub.iam.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// REST controller exposing read-only audit log endpoints.
// All routes are rooted at "/mediaHub/iam/auditLog" and return JSON wrapped in a status map.
@RestController
@RequestMapping("/mediaHub/iam/auditLog")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    // Service dependency that holds the audit log business/data-access logic.
    // Spring injects the concrete AuditLogService bean at startup via field injection.
    @Autowired
    private AuditLogService auditLogService;

    // GET all logs
    // Handles GET "/getAllLogs/v1.0" and fetches every audit log record from the service.
    // Returns HTTP 200 with a map containing a confirmation message and the full log list under "data".
    @GetMapping("/getAllLogs/v1.0")
    public ResponseEntity<Map<String, Object>> getAllLogs() {
        return ResponseEntity.ok(
            Map.of("message", "Audit logs retrieved",
                   "data", auditLogService.getAllLogs()));
    }

    // GET single log
    // Handles GET "/getLog/v1/{auditId}", reading the auditId from the URL path.
    // Looks up that single audit record via the service and returns it as HTTP 200 with a message.
    @GetMapping("/getLog/v1/{auditId}")
    public ResponseEntity<Map<String, Object>> getLog(
            @PathVariable Long auditId) {
        return ResponseEntity.ok(
            Map.of("message", "Audit log retrieved",
                   "data", auditLogService.getLogById(auditId)));
    }

    // GET logs by userId
    // Handles GET "/getUserLogs/v1/{userId}", taking the userId from the URL path.
    // Returns all audit logs tied to that user as HTTP 200, echoing back the userId alongside the data.
    @GetMapping("/getUserLogs/v1/{userId}")
    public ResponseEntity<Map<String, Object>> getUserLogs(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
            Map.of("message", "User audit logs retrieved",
                   "userId", userId,
                   "data", auditLogService.getLogsByUserId(userId)));
    }
}
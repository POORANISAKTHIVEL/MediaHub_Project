package com.mediahub.editorial.controller;

import com.mediahub.editorial.client.AuditClient;
import com.mediahub.editorial.model.PublicationSchedule;
import com.mediahub.editorial.service.PublicationScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/MediaHub/editorial")
public class PublicationScheduleController {

    @Autowired
    private PublicationScheduleService service;

    @Autowired
    private AuditClient auditClient;

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    // API 13 — POST /schedules
    @PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/schedules")
    public ResponseEntity<Map<String, Object>> createSchedule(
            @RequestBody PublicationSchedule schedule,
            Authentication authentication) {

        Map<String, Object> res =
                service.createSchedule(schedule, Long.valueOf(authentication.getName()));

        int code = (int) res.remove("statusCode");

        return ResponseEntity.status(code).body(res);
    }

    // API 14 — GET /schedules
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/schedules")
    public ResponseEntity<List<PublicationSchedule>>
            getAllSchedules() {

        return ResponseEntity.ok(
                service.getAllSchedules());
    }

    // API 15 — GET /schedules/{scheduleID}
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/schedules/{scheduleID}")
    public ResponseEntity<Map<String, Object>> getScheduleById(
            @PathVariable int scheduleID) {

        Map<String, Object> res =
                service.getScheduleById(scheduleID);

        int code = (int) res.remove("statusCode");

        return ResponseEntity.status(code).body(res);
    }

    // API 16 — POST /schedules/{scheduleID}/publish
    @PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/schedules/{scheduleID}/publish")
    public ResponseEntity<Map<String, Object>> publishSchedule(
            @PathVariable int scheduleID,
            Authentication authentication) {

        Map<String, Object> res =
                service.publishSchedule(scheduleID, Long.valueOf(authentication.getName()));

        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("SCHEDULE_PUBLISHED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "PublicationSchedule", String.valueOf(scheduleID),
                "Published schedule", "LOW");
        }

        return ResponseEntity.status(code).body(res);
    }

    // API 17 — POST /schedules/{scheduleID}/cancel
    @PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/schedules/{scheduleID}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSchedule(
            @PathVariable int scheduleID,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Map<String, Object> res =
                service.cancelSchedule(
                        scheduleID,
                        body.get("reason"), Long.valueOf(authentication.getName()));

        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("SCHEDULE_CANCELLED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "PublicationSchedule", String.valueOf(scheduleID),
                "Cancelled schedule: " + body.get("reason"), "MEDIUM");
        }

        return ResponseEntity.status(code).body(res);
    }

    // API 18 — DELETE /schedules/{scheduleID}
    @PreAuthorize("hasAuthority('editorial:manage')")
    @DeleteMapping("/schedules/{scheduleID}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @PathVariable int scheduleID,
            Authentication authentication) {

        Map<String, Object> res =
                service.deleteSchedule(scheduleID, Long.valueOf(authentication.getName()));

        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("SCHEDULE_DELETED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "PublicationSchedule", String.valueOf(scheduleID),
                "Deleted schedule", "MEDIUM");
        }

        return ResponseEntity.status(code).body(res);
    }

    // ✅ ANALYTICS API
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/analytics")
    public ResponseEntity<?> getEditorialAnalytics() {

        return ResponseEntity.ok(
                service.getEditorialAnalytics());
    }
}
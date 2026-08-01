package com.mediahub.editorial.controller;

import com.mediahub.editorial.model.PublicationSchedule;
import com.mediahub.editorial.service.PublicationScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/MediaHub/editorial")
public class PublicationScheduleController {

    @Autowired
    private PublicationScheduleService service;

    // API 13 — POST /schedules
    @PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/schedules")
    public ResponseEntity<Map<String, Object>> createSchedule(
            @RequestBody PublicationSchedule schedule) {

        Map<String, Object> res =
                service.createSchedule(schedule);

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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('content:publish')")
    @PostMapping("/schedules/{scheduleID}/publish")
    public ResponseEntity<Map<String, Object>> publishSchedule(
            @PathVariable int scheduleID) {

        Map<String, Object> res =
                service.publishSchedule(scheduleID);

        int code = (int) res.remove("statusCode");

        return ResponseEntity.status(code).body(res);
    }

    // API 17 — POST /schedules/{scheduleID}/cancel
    @PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/schedules/{scheduleID}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSchedule(
            @PathVariable int scheduleID,
            @RequestBody Map<String, String> body) {

        Map<String, Object> res =
                service.cancelSchedule(
                        scheduleID,
                        body.get("reason"));

        int code = (int) res.remove("statusCode");

        return ResponseEntity.status(code).body(res);
    }

    // API 18 — DELETE /schedules/{scheduleID}
    @PreAuthorize("hasAuthority('content:delete')")
    @DeleteMapping("/schedules/{scheduleID}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @PathVariable int scheduleID) {

        Map<String, Object> res =
                service.deleteSchedule(scheduleID);

        int code = (int) res.remove("statusCode");

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
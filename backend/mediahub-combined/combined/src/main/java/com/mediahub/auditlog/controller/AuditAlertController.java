package com.mediahub.auditlog.controller;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.AuditAlert;
import com.mediahub.auditlog.service.AuditAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/mediaHub/auditlog/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AuditAlertController {

    @Autowired private AuditAlertService alertService;

    // GET all alerts — paginated
    @GetMapping("/getAllAlerts/v1.0")
    public ResponseEntity<Map<String, Object>> getAllAlerts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditAlert> result =
            alertService.getAllAlerts(page, size);
        return ResponseEntity.ok(Map.of(
            "message",       "Alerts retrieved",
            "data",          result.getData(),
            "currentPage",   result.getCurrentPage(),
            "totalPages",    result.getTotalPages(),
            "totalElements", result.getTotalElements()));
    }

    // GET open alerts — paginated
    @GetMapping("/getOpenAlerts/v1.0")
    public ResponseEntity<Map<String, Object>> getOpenAlerts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditAlert> result =
            alertService.getOpenAlerts(page, size);
        return ResponseEntity.ok(Map.of(
            "message",       "Open alerts retrieved",
            "data",          result.getData(),
            "currentPage",   result.getCurrentPage(),
            "totalPages",    result.getTotalPages(),
            "totalElements", result.getTotalElements()));
    }

    // GET single alert
    @GetMapping("/getAlert/v1/{alertId}")
    public ResponseEntity<Map<String, Object>> getAlert(
            @PathVariable Long alertId) {
        return ResponseEntity.ok(Map.of(
            "message", "Alert retrieved",
            "data",    alertService.getAlertById(alertId)));
    }
}
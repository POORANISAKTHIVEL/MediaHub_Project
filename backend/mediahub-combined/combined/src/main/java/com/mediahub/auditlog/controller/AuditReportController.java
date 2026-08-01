package com.mediahub.auditlog.controller;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.AuditReport;
import com.mediahub.auditlog.service.AuditReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/mediaHub/auditlog/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AuditReportController {

    @Autowired private AuditReportService reportService;

    // GET all reports — paginated
    @GetMapping("/getAllReports/v1.0")
    public ResponseEntity<Map<String, Object>> getAllReports(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditReport> result =
            reportService.getAllReports(page, size);
        return ResponseEntity.ok(Map.of(
            "message",       "Reports retrieved",
            "data",          result.getData(),
            "currentPage",   result.getCurrentPage(),
            "totalPages",    result.getTotalPages(),
            "totalElements", result.getTotalElements()));
    }

    // GET single report
    @GetMapping("/getReport/v1/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(Map.of(
            "message", "Report retrieved",
            "data",    reportService.getReportById(reportId)));
    }

    // POST generate report
    @PostMapping("/generateReport/v1.0")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestBody Map<String, Object> body) {
        AuditReport report = reportService.generateReport(
            (String) body.get("reportName"),
            AuditReport.ReportType.valueOf(
                (String) body.get("reportType")),
            (String) body.getOrDefault("moduleScope", "ALL"),
            LocalDateTime.parse((String) body.get("fromDate")),
            LocalDateTime.parse((String) body.get("toDate")),
            Long.valueOf(body.get("generatedBy").toString()));
        return ResponseEntity.status(201).body(Map.of(
            "message", "Report generated successfully",
            "data",    report));
    }
}
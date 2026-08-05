package com.mediahub.analytics.controller;

import com.mediahub.analytics.dto.ReportResponse;
import com.mediahub.analytics.service.AnalyticsService;
import com.mediahub.analytics.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/mediaHub/reports")
public class ReportController {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private AnalyticsService analyticsService;

    // ── POST /mediaHub/reports/generate ──────────────────────────────────────
    // analyticsService.getAnalytics(...) makes blocking WebClient calls internally, same as
    // AnalyticsController#getDashboard. Without subscribeOn(Schedulers.boundedElastic()) those
    // .block() calls run on a Netty/Reactor event-loop thread, which throws
    // "block()/blockFirst()/blockLast() are blocking, which is not supported" and silently falls
    // back to an empty analytics map for every sub-service — every generated report ends up with
    // totalContents stuck at 0 even though real data exists.
    @PreAuthorize("hasAuthority('report:view')")
    @PostMapping("/generate")
    public Mono<ResponseEntity<ReportResponse>> generateReport(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("POST /mediaHub/reports/generate - Generating report");
        return Mono.fromCallable(() -> reportService.generateReport(analyticsService.getAnalytics(authorization)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(report -> {
                    logger.info("Report generated with ID: {}", report.getReportId());
                    return ResponseEntity.status(201).body(report);
                });
    }

    // ── GET /mediaHub/reports/{id} ────────────────────────────────────────────
    // Reactive method security (@PreAuthorize in a WebFlux app) requires the handler to return
    // a Mono/Flux so the security context has something to attach to — a plain ResponseEntity
    // throws "must return an instance of Publisher... to support Reactor Context" on every call.
    // Same fix as generateReport below, applied consistently to every @PreAuthorize'd method here.
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReportResponse>> getReportById(@PathVariable Long id) {
        logger.info("GET /mediaHub/reports/{} - Fetching report", id);
        return Mono.fromCallable(() -> reportService.getReportById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(report -> {
                    if (report == null) {
                        logger.warn("Report not found with ID: {}", id);
                        return ResponseEntity.notFound().<ReportResponse>build();
                    }
                    return ResponseEntity.ok(report);
                });
    }

    // ── DELETE /mediaHub/reports/{id} ─────────────────────────────────────────
    @PreAuthorize("hasAuthority('report:view')")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteReport(@PathVariable Long id) {
        logger.info("DELETE /mediaHub/reports/{} - Deleting report", id);
        return Mono.fromCallable(() -> reportService.deleteReport(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    // ── GET /mediaHub/reports/download/{id} ───────────────────────────────────
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<byte[]>> downloadReport(@PathVariable Long id) {
        logger.info("GET /mediaHub/reports/download/{} - Downloading report", id);
        return Mono.fromCallable(() -> reportService.downloadReport(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(reportData -> ResponseEntity.ok()
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=MediaHub_Analytics_Report_" + id + ".xlsx")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(reportData));
    }
}

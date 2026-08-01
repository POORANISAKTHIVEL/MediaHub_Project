package com.mediahub.analytics.controller;

import com.mediahub.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@RestController
@RequestMapping("/mediaHub/analytics")
public class AnalyticsController {

    private static final Logger logger =
            LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AnalyticsService service;

    // ── GET /mediaHub/analytics/dashboard ────────────────────────────────────
    // Aggregated dashboard from all 7 modules
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/dashboard")
    public Mono<ResponseEntity<Map<String, Object>>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("GET /mediaHub/analytics/dashboard - Fetching full dashboard");
        return Mono.fromCallable(() -> service.getAnalytics(authorization))
                .subscribeOn(Schedulers.boundedElastic())
                .map(dashboard -> {
                    logger.info("Dashboard assembled successfully");
                    return ResponseEntity.ok(dashboard);
                });
    }

    // ── GET /mediaHub/analytics/reports/summary ───────────────────────────────
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/reports/summary")
    public Mono<ResponseEntity<Map<String, Object>>> getSummaryReport(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("GET /mediaHub/analytics/reports/summary");
        return Mono.fromCallable(() -> service.getAnalytics(authorization))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}

package com.mediahub.auditlog.service;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.AuditAlert;
import com.mediahub.auditlog.repository.AuditAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditAlertService {

    @Autowired private AuditAlertRepository alertRepository;

    // ── GET all alerts — paginated ─────────────────────────────────────────
    public PageResponse<AuditAlert> getAllAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(alertRepository.findAll(pageable));
    }

    // ── GET open alerts — paginated ────────────────────────────────────────
    public PageResponse<AuditAlert> getOpenAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(alertRepository.findByStatus(
            AuditAlert.AlertStatus.OPEN, pageable));
    }

    // ── GET alerts by severity — paginated ────────────────────────────────
    public PageResponse<AuditAlert> getAlertsBySeverity(
            AuditAlert.Severity severity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            alertRepository.findBySeverity(severity, pageable));
    }

    // ── GET single alert ───────────────────────────────────────────────────
    public AuditAlert getAlertById(Long alertId) {
        return alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("ALERT_NOT_FOUND"));
    }
}
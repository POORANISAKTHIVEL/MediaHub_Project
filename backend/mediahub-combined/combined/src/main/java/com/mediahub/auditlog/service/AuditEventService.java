package com.mediahub.auditlog.service;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.AuditAlert;
import com.mediahub.auditlog.entity.AuditEvent;
import com.mediahub.auditlog.repository.AuditAlertRepository;
import com.mediahub.auditlog.repository.AuditEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditEventService {

    @Autowired private AuditEventRepository auditEventRepository;
    @Autowired private AuditAlertRepository auditAlertRepository;

    // ── GET all events — paginated ─────────────────────────────────────────
    public PageResponse<AuditEvent> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        Page<AuditEvent> result = auditEventRepository.findAll(pageable);
        return PageResponse.of(result);
    }

    // ── GET single event ───────────────────────────────────────────────────
    public AuditEvent getEventById(Long eventId) {
        return auditEventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("AUDIT_EVENT_NOT_FOUND"));
    }

    // ── GET events by module — paginated ───────────────────────────────────
    public PageResponse<AuditEvent> getEventsByModule(
            AuditEvent.ModuleSource moduleSource, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            auditEventRepository.findByModuleSource(moduleSource, pageable));
    }

    // ── GET events by user — paginated ─────────────────────────────────────
    public PageResponse<AuditEvent> getEventsByUser(
            Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            auditEventRepository.findByPerformedBy(userId, pageable));
    }

    // ── GET events by severity — paginated ────────────────────────────────
    public PageResponse<AuditEvent> getEventsBySeverity(
            AuditEvent.Severity severity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            auditEventRepository.findBySeverity(severity, pageable));
    }

    // ── GET events by date range — paginated ──────────────────────────────
    public PageResponse<AuditEvent> getEventsByDateRange(
            LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());
        return PageResponse.of(
            auditEventRepository.findByCreatedAtBetween(from, to, pageable));
    }

    // ── GET events by entity — not paginated (usually small result set) ───
    public List<AuditEvent> getEventsByEntity(
            String entityType, String entityId) {
        return auditEventRepository
            .findByTargetEntityTypeAndTargetEntityId(entityType, entityId);
    }

    // ── POST log event ────────────────────────────────────────────────────
    public AuditEvent logEvent(String eventType,
                               AuditEvent.ModuleSource moduleSource,
                               Long performedBy,
                               String performedByRole,
                               String targetEntityType,
                               String targetEntityId,
                               String oldValue,
                               String newValue,
                               String ipAddress,
                               AuditEvent.Severity severity,
                               AuditEvent.EventStatus status,
                               String description) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setModuleSource(moduleSource);
        event.setPerformedBy(performedBy);
        event.setPerformedByRole(performedByRole);
        event.setTargetEntityType(targetEntityType);
        event.setTargetEntityId(targetEntityId);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        event.setIpAddress(ipAddress);
        event.setSeverity(severity);
        event.setStatus(status);
        event.setDescription(description);

        AuditEvent saved = auditEventRepository.save(event);

        // Auto-raise alert for HIGH and CRITICAL events
        if (severity == AuditEvent.Severity.HIGH ||
            severity == AuditEvent.Severity.CRITICAL) {
            raiseAlert(saved);
        }
        return saved;
    }

    private void raiseAlert(AuditEvent event) {
        AuditAlert alert = new AuditAlert();
        alert.setEventId(event.getEventId());
        alert.setAlertType(AuditAlert.AlertType.POLICY_BREACH);
        alert.setAlertMessage(
            "High severity event detected: " + event.getEventType() +
            " in module " + event.getModuleSource());
        alert.setSeverity(AuditAlert.Severity.valueOf(
            event.getSeverity().name()));
        alert.setTriggeredBy("AUTO_ALERT_SYSTEM");
        alert.setStatus(AuditAlert.AlertStatus.OPEN);
        auditAlertRepository.save(alert);
    }
}
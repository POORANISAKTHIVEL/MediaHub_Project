package com.mediahub.auditlog.repository;

import com.mediahub.auditlog.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findByPerformedBy(Long performedBy, Pageable pageable);
    Page<AuditEvent> findByModuleSource(AuditEvent.ModuleSource moduleSource, Pageable pageable);
    Page<AuditEvent> findByEventType(String eventType, Pageable pageable);
    Page<AuditEvent> findBySeverity(AuditEvent.Severity severity, Pageable pageable);
    Page<AuditEvent> findByStatus(AuditEvent.EventStatus status, Pageable pageable);
    Page<AuditEvent> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<AuditEvent> findByTargetEntityTypeAndTargetEntityId(String targetEntityType, String targetEntityId);
    // Non-paginated — kept for report generation internal use
    List<AuditEvent> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
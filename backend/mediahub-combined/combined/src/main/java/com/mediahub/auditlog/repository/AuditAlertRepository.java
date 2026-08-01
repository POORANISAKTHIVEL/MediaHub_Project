package com.mediahub.auditlog.repository;

import com.mediahub.auditlog.entity.AuditAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditAlertRepository
        extends JpaRepository<AuditAlert, Long> {
    Page<AuditAlert> findByStatus(AuditAlert.AlertStatus status, Pageable pageable);
    Page<AuditAlert> findBySeverity(AuditAlert.Severity severity, Pageable pageable);
    List<AuditAlert> findByEventId(Long eventId);
}
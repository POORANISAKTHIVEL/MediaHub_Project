package com.mediahub.auditlog.repository;

import com.mediahub.auditlog.entity.AuditReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditReportRepository
        extends JpaRepository<AuditReport, Long> {
    Page<AuditReport> findByGeneratedBy(Long generatedBy, Pageable pageable);
    Page<AuditReport> findByStatus(AuditReport.ReportStatus status, Pageable pageable);
    Page<AuditReport> findByReportType(AuditReport.ReportType reportType, Pageable pageable);
}
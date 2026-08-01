package com.mediahub.auditlog.service;

import com.mediahub.auditlog.dto.PageResponse;
import com.mediahub.auditlog.entity.AuditEvent;
import com.mediahub.auditlog.entity.AuditReport;
import com.mediahub.auditlog.repository.AuditEventRepository;
import com.mediahub.auditlog.repository.AuditReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditReportService {

    @Autowired private AuditReportRepository reportRepository;
    @Autowired private AuditEventRepository  eventRepository;

    // ── GET all reports — paginated ────────────────────────────────────────
    public PageResponse<AuditReport> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by("generatedAt").descending());
        return PageResponse.of(reportRepository.findAll(pageable));
    }

    // ── GET single report ──────────────────────────────────────────────────
    public AuditReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("REPORT_NOT_FOUND"));
    }

    // ── POST generate report ───────────────────────────────────────────────
    public AuditReport generateReport(String reportName,
                                       AuditReport.ReportType reportType,
                                       String moduleScope,
                                       LocalDateTime fromDate,
                                       LocalDateTime toDate,
                                       Long generatedBy) {
        List<AuditEvent> events =
            eventRepository.findByCreatedAtBetween(fromDate, toDate);

        int total    = events.size();
        int critical = (int) events.stream()
            .filter(e -> e.getSeverity() == AuditEvent.Severity.CRITICAL)
            .count();
        int failed   = (int) events.stream()
            .filter(e -> e.getStatus() == AuditEvent.EventStatus.FAILED)
            .count();

        AuditReport report = new AuditReport();
        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setModuleScope(moduleScope);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setTotalEvents(total);
        report.setCriticalEvents(critical);
        report.setFailedEvents(failed);
        report.setGeneratedBy(generatedBy);
        report.setStatus(AuditReport.ReportStatus.GENERATED);
        report.setSummary(
            "{\"total\":" + total +
            ",\"critical\":" + critical +
            ",\"failed\":" + failed + "}");

        return reportRepository.save(report);
    }
}
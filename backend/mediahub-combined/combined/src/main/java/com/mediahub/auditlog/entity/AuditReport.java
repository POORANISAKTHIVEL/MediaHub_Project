package com.mediahub.auditlog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_report")
public class AuditReport {

    public enum ReportType { DAILY, WEEKLY, MONTHLY, QUARTERLY, CUSTOM }
    public enum ReportStatus { PENDING, GENERATED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false, length = 120)
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false, length = 255)
    private String moduleScope = "ALL";

    @Column(nullable = false)
    private LocalDateTime fromDate;

    @Column(nullable = false)
    private LocalDateTime toDate;

    private Integer totalEvents = 0;
    private Integer criticalEvents = 0;
    private Integer failedEvents = 0;
    private Integer alertsRaised = 0;

    @Column(nullable = false)
    private Long generatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(length = 255)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(columnDefinition = "JSON")
    private String summary;

    @PrePersist
    void prePersist() { generatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    public String getModuleScope() { return moduleScope; }
    public void setModuleScope(String moduleScope) { this.moduleScope = moduleScope; }
    public LocalDateTime getFromDate() { return fromDate; }
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    public LocalDateTime getToDate() { return toDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
    public Integer getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Integer totalEvents) { this.totalEvents = totalEvents; }
    public Integer getCriticalEvents() { return criticalEvents; }
    public void setCriticalEvents(Integer criticalEvents) { this.criticalEvents = criticalEvents; }
    public Integer getFailedEvents() { return failedEvents; }
    public void setFailedEvents(Integer failedEvents) { this.failedEvents = failedEvents; }
    public Integer getAlertsRaised() { return alertsRaised; }
    public void setAlertsRaised(Integer alertsRaised) { this.alertsRaised = alertsRaised; }
    public Long getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(Long generatedBy) { this.generatedBy = generatedBy; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
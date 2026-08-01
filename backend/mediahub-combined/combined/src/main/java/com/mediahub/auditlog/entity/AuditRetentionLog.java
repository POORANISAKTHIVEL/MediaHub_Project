package com.mediahub.auditlog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_retention_log")
public class AuditRetentionLog {

    public enum RetentionStatus { SUCCESS, FAILED, PARTIAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long retentionId;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private LocalDateTime purgeFromDate;

    @Column(nullable = false)
    private LocalDateTime purgeToDate;

    private Integer recordsPurged = 0;

    @Column(nullable = false)
    private Long purgedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purgedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(length = 255)
    private String archivePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetentionStatus status = RetentionStatus.SUCCESS;

    @PrePersist
    void prePersist() { purgedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getRetentionId() { return retentionId; }
    public void setRetentionId(Long retentionId) { this.retentionId = retentionId; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public LocalDateTime getPurgeFromDate() { return purgeFromDate; }
    public void setPurgeFromDate(LocalDateTime purgeFromDate) { this.purgeFromDate = purgeFromDate; }
    public LocalDateTime getPurgeToDate() { return purgeToDate; }
    public void setPurgeToDate(LocalDateTime purgeToDate) { this.purgeToDate = purgeToDate; }
    public Integer getRecordsPurged() { return recordsPurged; }
    public void setRecordsPurged(Integer recordsPurged) { this.recordsPurged = recordsPurged; }
    public Long getPurgedBy() { return purgedBy; }
    public void setPurgedBy(Long purgedBy) { this.purgedBy = purgedBy; }
    public LocalDateTime getPurgedAt() { return purgedAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getArchivePath() { return archivePath; }
    public void setArchivePath(String archivePath) { this.archivePath = archivePath; }
    public RetentionStatus getStatus() { return status; }
    public void setStatus(RetentionStatus status) { this.status = status; }
}
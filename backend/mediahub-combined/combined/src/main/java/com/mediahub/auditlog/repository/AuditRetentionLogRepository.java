package com.mediahub.auditlog.repository;

import com.mediahub.auditlog.entity.AuditRetentionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditRetentionLogRepository
    extends JpaRepository<AuditRetentionLog, Long> {
    List<AuditRetentionLog> findByPolicyId(Long policyId);
    List<AuditRetentionLog> findByPurgedBy(Long purgedBy);
    List<AuditRetentionLog> findByStatus(AuditRetentionLog.RetentionStatus status);
}
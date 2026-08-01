package com.mediahub.iam.service;

import com.mediahub.iam.entity.AuditLog;
import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.AuditAction;
import com.mediahub.iam.repository.AuditLogRepository;
import com.mediahub.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    // ── GET all logs ──────────────────────────────────────────────────────────
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    // ── GET single log ────────────────────────────────────────────────────────
    public AuditLog getLogById(Long auditId) {
        return auditLogRepository.findById(auditId)
            .orElseThrow(() -> new RuntimeException("AUDIT_LOG_NOT_FOUND"));
    }

    // ── GET logs by userId ────────────────────────────────────────────────────
    public List<AuditLog> getLogsByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return auditLogRepository.findByUser(user);
    }

    // ── Internal log writer ───────────────────────────────────────────────────
    public void log(Long performedByUserId, AuditAction action,
                    String entityType, String entityId,
                    String oldValue, String newValue, String ipAddress) {
        User user = userRepository.findById(performedByUserId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}
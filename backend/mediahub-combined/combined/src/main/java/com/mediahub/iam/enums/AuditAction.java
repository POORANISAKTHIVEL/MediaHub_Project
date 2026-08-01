package com.mediahub.iam.enums;

// Enum listing the fixed set of action types that can be recorded in an audit log entry.
// Used by the AuditLog entity (stored as a String) to categorize what operation occurred.
public enum AuditAction {
    created,    // a new record/entity was created
    updated,    // an existing record was modified
    deleted,    // a record was deleted (or soft-deleted)
    suspended,  // a user/entity was suspended
    activated,  // a previously suspended/deactivated entity was re-activated
    approved,   // a request/entity was approved
    rejected,   // a request/entity was rejected
    finalised   // a process/entity reached its final, locked state
}
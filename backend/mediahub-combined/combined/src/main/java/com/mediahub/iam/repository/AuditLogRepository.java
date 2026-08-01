package com.mediahub.iam.repository;

import com.mediahub.iam.entity.AuditLog;
import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Spring Data JPA repository for AuditLog entities (primary key type Long).
// Extending JpaRepository provides ready-made CRUD/paging methods; Spring auto-implements this interface at runtime.
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Derived query: returns all audit logs belonging to the given user.
    // Spring generates the SQL automatically from the method name ("findBy" + field "User").
    List<AuditLog> findByUser(User user);

    // Derived query: returns all audit logs referencing the given affected entity id.
    List<AuditLog> findByEntityId(String entityId);

    // Derived query: returns all audit logs that recorded the given action type.
    List<AuditLog> findByAction(AuditAction action);
}
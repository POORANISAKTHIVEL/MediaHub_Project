package com.mediahub.iam.repository;

import com.mediahub.iam.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Spring Data JPA repository for Role entities (primary key type Long).
// Extending JpaRepository provides ready-made CRUD/paging methods; Spring auto-implements this interface at runtime.
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Derived query: looks up a role by its unique type/name.
    // Returns an Optional (empty if none matches), so callers can handle the "not found" case safely.
    Optional<Role> findByRoleType(String roleType);
}
package com.mediahub.iam.repository;

import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Spring Data JPA repository for User entities (primary key type Long).
// Extending JpaRepository provides ready-made CRUD/paging methods; Spring auto-implements this interface at runtime.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Derived query: looks up a user by email (the unique login identifier).
    // Returns an Optional (empty if none matches), so callers can handle the "not found" case safely.
    Optional<User> findByEmail(String email);

    // Derived query: returns all users whose token/session has been revoked (isRevoked = true).
    List<User> findByIsRevokedTrue();

    // Derived query: returns all users currently in the given status (e.g. active, suspended, inactive).
    List<User> findByStatus(UserStatus status);
}
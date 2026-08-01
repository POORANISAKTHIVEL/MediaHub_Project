package com.mediahub.iam.repository;

import com.mediahub.iam.entity.Permission;
import com.mediahub.iam.entity.Role;
import com.mediahub.iam.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Spring Data JPA repository for RolePermission join entities (primary key type Long).
// Extending JpaRepository provides ready-made CRUD methods; Spring auto-implements this interface at runtime.
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    // Derived query: returns all role-permission links for the given role (i.e. that role's permissions).
    List<RolePermission> findByRole(Role role);

    // Derived query: finds the specific link between a role and a permission, if it exists.
    // Returns an Optional so callers can check whether that permission is actually assigned to the role.
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    // Derived delete: removes all permission links for the given role (e.g. when a role is deleted).
    void deleteByRole(Role role);

    // Derived delete: removes the single link between a specific role and permission (revoking it).
    void deleteByRoleAndPermission(Role role, Permission permission);

    // Derived delete: removes all role-permission links for the given permission.
    void deleteByPermission(Permission permission);
}
package com.mediahub.iam.service;

import com.mediahub.iam.entity.Permission;
import com.mediahub.iam.entity.Role;
import com.mediahub.iam.entity.RolePermission;
import com.mediahub.iam.repository.PermissionRepository;
import com.mediahub.iam.repository.RolePermissionRepository;
import com.mediahub.iam.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    // ── GET all roles ─────────────────────────────────────────────────────────
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // ── GET single role ───────────────────────────────────────────────────────
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("ROLE_NOT_FOUND"));
    }

    // ── GET permissions for a role ────────────────────────────────────────────
    public List<String> getPermissionsForRole(Long roleId) {
        Role role = getRoleById(roleId);
        return rolePermissionRepository.findByRole(role)
            .stream()
            .map(rp -> rp.getPermission().getPermissionType())
            .collect(Collectors.toList());
    }

    // ── POST create role ──────────────────────────────────────────────────────
    public Role createRole(String roleType) {
        if (roleRepository.findByRoleType(roleType).isPresent()) {
            throw new RuntimeException("ROLE_ALREADY_EXISTS");
        }
        Role role = new Role();
        role.setRoleType(roleType);
        return roleRepository.save(role);
    }

    // ── PUT update role ───────────────────────────────────────────────────────
    public Role updateRole(Long roleId, String roleType) {
        Role role = getRoleById(roleId);
        role.setRoleType(roleType);
        return roleRepository.save(role);
    }

    // ── DELETE role ───────────────────────────────────────────────────────────
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = getRoleById(roleId);
        rolePermissionRepository.deleteByRole(role);
        roleRepository.delete(role);
    }

    // ── POST assign permission ────────────────────────────────────────────────
    public RolePermission assignPermission(Long roleId, Long permissionId) {
        Role role = getRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("PERMISSION_NOT_FOUND"));
        if (rolePermissionRepository.findByRoleAndPermission(role, permission).isPresent()) {
            throw new RuntimeException("PERMISSION_ALREADY_ASSIGNED");
        }
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        return rolePermissionRepository.save(rp);
    }

    // ── DELETE revoke permission from role (transactional) ──────────────────
    @Transactional
    public void revokePermission(Long roleId, Long permissionId) {
        Role role = getRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("PERMISSION_NOT_FOUND"));
        RolePermission rp = rolePermissionRepository.findByRoleAndPermission(role, permission)
            .orElseThrow(() -> new RuntimeException("PERMISSION_NOT_ASSIGNED"));
        rolePermissionRepository.delete(rp);
    }
}
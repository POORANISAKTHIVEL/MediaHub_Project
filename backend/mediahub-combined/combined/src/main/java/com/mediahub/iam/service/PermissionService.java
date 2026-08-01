package com.mediahub.iam.service;

import com.mediahub.iam.entity.Permission;
import com.mediahub.iam.repository.PermissionRepository;
import com.mediahub.iam.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    // ── GET all permissions ───────────────────────────────────────────────────
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // ── GET single permission ─────────────────────────────────────────────────
    public Permission getPermissionById(Long permissionId) {
        return permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("PERMISSION_NOT_FOUND"));
    }

    // ── POST create permission ────────────────────────────────────────────────
    public Permission createPermission(String permissionType) {
        if (permissionRepository.findByPermissionType(permissionType).isPresent()) {
            throw new RuntimeException("PERMISSION_ALREADY_EXISTS");
        }
        Permission permission = new Permission();
        permission.setPermissionType(permissionType);
        return permissionRepository.save(permission);
    }

    // ── PUT update permission ─────────────────────────────────────────────────
    public Permission updatePermission(Long permissionId, String permissionType) {
        Permission permission = getPermissionById(permissionId);
        permission.setPermissionType(permissionType);
        return permissionRepository.save(permission);
    }

    // ── DELETE permission ─────────────────────────────────────────────────────
    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("PERMISSION_NOT_FOUND"));
        // remove any role-permission mappings first to avoid FK constraint errors
        rolePermissionRepository.deleteByPermission(permission);
        permissionRepository.delete(permission);
    }
}
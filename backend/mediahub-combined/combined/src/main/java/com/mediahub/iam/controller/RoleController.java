package com.mediahub.iam.controller;

import com.mediahub.auditlog.entity.AuditEvent;
import com.mediahub.iam.client.AuditClient;
import com.mediahub.iam.dto.AssignPermissionRequest;
import com.mediahub.iam.entity.Permission;
import com.mediahub.iam.entity.Role;
import com.mediahub.iam.repository.RolePermissionRepository;
import com.mediahub.iam.service.PermissionService;
import com.mediahub.iam.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// REST controller managing roles and their permission assignments.
// All routes are rooted at "/mediaHub/iam/roles" and return JSON wrapped in a status map.
@RestController
@RequestMapping("/mediaHub/iam/roles")
public class RoleController {

    // Spring-injected dependencies: the role-permission join repository plus the role and
    // permission services. These supply the data-access and business logic this controller delegates to.
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private AuditClient auditClient;

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    // GET all roles
    // Handles GET "/getAllRoles/v1.0" and fetches every role record from the service.
    // Returns HTTP 200 with a message and the full role list under "data".
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllRoles/v1.0")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        return ResponseEntity.ok(
            Map.of("message", "Roles retrieved",
                   "data", roleService.getAllRoles()));
    }

    // GET single role
    // Handles GET "/getRole/v1/{roleId}", reading the id from the URL path.
    // Looks up that one role via the service and returns it as HTTP 200 with a message.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getRole/v1/{roleId}")
    public ResponseEntity<Map<String, Object>> getRole(
            @PathVariable Long roleId) {
        return ResponseEntity.ok(
            Map.of("message", "Role retrieved",
                   "data", roleService.getRoleById(roleId)));
    }

    // GET permissions for role
    // Handles GET "/getPermissions/v1/{roleId}", taking the role id from the URL path.
    // Returns HTTP 200 with the list of permissions tied to that role, echoing the roleId back.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getPermissions/v1/{roleId}")
    public ResponseEntity<Map<String, Object>> getPermissions(
            @PathVariable Long roleId) {
        return ResponseEntity.ok(
            Map.of("message", "Permissions retrieved",
                   "roleId", roleId,
                   "permissions", roleService.getPermissionsForRole(roleId)));
    }

    // POST create role
    // Handles POST "/createRole/v1.0", reading the "roleType" from the JSON body.
    // Asks the service to create the new role and returns HTTP 201 (Created) on success.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createRole/v1.0")
    public ResponseEntity<Map<String, String>> createRole(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        roleService.createRole(body.get("roleType"));
        auditClient.log("ROLE_CREATED", Long.valueOf(authentication.getName()), actorRole(authentication),
            "Role", body.get("roleType"), "Created role: " + body.get("roleType"), AuditEvent.Severity.MEDIUM);
        return ResponseEntity.status(201).body(
            Map.of("message", "Role created successfully"));
    }

    // PUT update role
    // Handles PUT "/updateRole/v1/{roleId}", taking the id from the path and the new type from the body.
    // Updates the matching role via the service and returns HTTP 200 with a confirmation message.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateRole/v1/{roleId}")
    public ResponseEntity<Map<String, String>> updateRole(
            @PathVariable Long roleId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        roleService.updateRole(roleId, body.get("roleType"));
        auditClient.log("ROLE_UPDATED", Long.valueOf(authentication.getName()), actorRole(authentication),
            "Role", roleId.toString(), "Updated role to: " + body.get("roleType"), AuditEvent.Severity.MEDIUM);
        return ResponseEntity.ok(
            Map.of("message", "Role updated successfully"));
    }

    // DELETE revoke permission from role
    // Handles DELETE "/revokePermission/v1/{roleId}/{permissionId}", resolving both entities by id.
    // Verifies the permission is actually assigned (else throws), then deletes the role-permission link.
    // Returns HTTP 200 with a confirmation message once the mapping is removed.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/revokePermission/v1/{roleId}/{permissionId}")
    public ResponseEntity<Map<String, String>> revokePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId,
            Authentication authentication) {
        roleService.revokePermission(roleId, permissionId);
        auditClient.log("PERMISSION_REVOKED", Long.valueOf(authentication.getName()), actorRole(authentication),
            "Role", roleId.toString(), "Revoked permission id " + permissionId + " from role " + roleId,
            AuditEvent.Severity.MEDIUM);
        return ResponseEntity.ok(Map.of("message", "Permission revoked successfully"));
    }

    // DELETE role
    // Handles DELETE "/deleteRole/v1/{roleId}" and permanently removes the role.
    // Unlinks related role-permission mappings first to avoid FK constraint failures.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteRole/v1/{roleId}")
    public ResponseEntity<Map<String, String>> deleteRole(
            @PathVariable Long roleId,
            Authentication authentication) {
        roleService.deleteRole(roleId);
        auditClient.log("ROLE_DELETED", Long.valueOf(authentication.getName()), actorRole(authentication),
            "Role", roleId.toString(), "Deleted role", AuditEvent.Severity.MEDIUM);
        return ResponseEntity.ok(
            Map.of("message", "Role deleted. Permissions unlinked automatically."));
    }

    // POST assign permission
    // Handles POST "/assignPermission/v1/{roleId}", reading the role id from the path and the
    // permission id from the AssignPermissionRequest body. Links the permission to the role via the
    // service and returns HTTP 201 (Created) on success.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assignPermission/v1/{roleId}")
    public ResponseEntity<Map<String, String>> assignPermission(
            @PathVariable Long roleId,
            @RequestBody AssignPermissionRequest request,
            Authentication authentication) {
        roleService.assignPermission(roleId, request.getPermissionId());
        auditClient.log("PERMISSION_ASSIGNED", Long.valueOf(authentication.getName()), actorRole(authentication),
            "Role", roleId.toString(), "Assigned permission id " + request.getPermissionId() + " to role " + roleId,
            AuditEvent.Severity.MEDIUM);
        return ResponseEntity.status(201).body(
            Map.of("message", "Permission assigned successfully"));
    }
}
package com.mediahub.iam.controller;

import com.mediahub.iam.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// REST controller exposing CRUD endpoints for permission records.
// All routes are rooted at "/mediaHub/iam/permissions" and return JSON wrapped in a status map.
@RestController
@RequestMapping("/mediaHub/iam/permissions")
public class PermissionController {

    // Service dependency holding the permission business/data-access logic.
    // Spring injects the PermissionService bean automatically at startup via field injection.
    @Autowired
    private PermissionService permissionService;

    // GET all permissions
    // Handles GET "/getAllPermissions/v1.0" and fetches every permission record from the service.
    // Returns HTTP 200 with a message and the full permission list under "data".
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllPermissions/v1.0")
    public ResponseEntity<Map<String, Object>> getAllPermissions() {
        return ResponseEntity.ok(
            Map.of("message", "Permissions retrieved",
                   "data", permissionService.getAllPermissions()));
    }

    // GET single permission
    // Handles GET "/getPermission/v1/{permissionId}", reading the id from the URL path.
    // Looks up that one permission via the service and returns it as HTTP 200 with a message.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getPermission/v1/{permissionId}")
    public ResponseEntity<Map<String, Object>> getPermission(
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(
            Map.of("message", "Permission retrieved",
                   "data", permissionService.getPermissionById(permissionId)));
    }

    // POST create permission
    // Handles POST "/createPermission/v1.0", reading the "permissionType" from the JSON body.
    // Asks the service to create the new permission and returns HTTP 201 (Created) on success.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createPermission/v1.0")
    public ResponseEntity<Map<String, String>> createPermission(
            @RequestBody Map<String, String> body) {
        permissionService.createPermission(body.get("permissionType"));
        return ResponseEntity.status(201).body(
            Map.of("message", "Permission created successfully"));
    }

    // PUT update permission
    // Handles PUT "/updatePermission/v1/{permissionId}", taking the id from the path and the new type from the body.
    // Updates the matching permission via the service and returns HTTP 200 with a confirmation message.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updatePermission/v1/{permissionId}")
    public ResponseEntity<Map<String, String>> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody Map<String, String> body) {
        permissionService.updatePermission(permissionId,
            body.get("permissionType"));
        return ResponseEntity.ok(
            Map.of("message", "Permission updated successfully"));
    }

    // DELETE permission
    // Handles DELETE "/deletePermission/v1/{permissionId}", reading the id from the URL path.
    // Removes that permission via the service and returns HTTP 200 with a confirmation message.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deletePermission/v1/{permissionId}")
    public ResponseEntity<Map<String, String>> deletePermission(
            @PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.ok(
            Map.of("message", "Permission deleted successfully"));
    }
}
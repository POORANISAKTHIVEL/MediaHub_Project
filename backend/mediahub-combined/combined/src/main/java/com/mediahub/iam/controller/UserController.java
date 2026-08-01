package com.mediahub.iam.controller;

import com.mediahub.iam.dto.ActivateRequest;
import com.mediahub.iam.dto.SuspendRequest;
import com.mediahub.iam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// REST controller managing user accounts and their lifecycle (read, update, deactivate, suspend, activate).
// All routes are rooted at "/mediaHub/iam/users" and return JSON wrapped in a status map.
@RestController
@RequestMapping("/mediaHub/iam/users")
public class UserController {

    // Service dependency holding the user business/data-access logic.
    // Spring injects the UserService bean automatically at startup via field injection.
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // GET all users
    // Handles GET "/getAllUsers/v1.0" and fetches every user record from the service.
    // Returns HTTP 200 with a message and the full user list under "data".
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers/v1.0")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(
            Map.of("message", "Users retrieved",
                   "data", userService.getAllUsers()));
    }

    // GET single user
    // Handles GET "/getUser/v1/{userId}", reading the id from the URL path.
    // Looks up that one user via the service and returns it as HTTP 200 with a message.
    // principal is a String (JWT subject) while userId is a Long — compare as strings, otherwise
    // a non-admin user can never view/update their own profile (String never equals Long).
    @PreAuthorize("hasRole('ADMIN') or principal.toString() == #userId.toString()")
    @GetMapping("/getUser/v1/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(
            @PathVariable Long userId) {
        log.info("Fetching user with id={}", userId);
        return ResponseEntity.ok(
            Map.of("message", "User retrieved",
                   "data", userService.getUserById(userId)));
    }

    // PUT update user
    // Handles PUT "/updateUser/v1/{userId}", taking the id from the path and name/phone/country from the body.
    // Applies the profile changes via the service and returns HTTP 200 with a confirmation message.
    // principal is a String (JWT subject) while userId is a Long — compare as strings, otherwise
    // a non-admin user can never view/update their own profile (String never equals Long).
    @PreAuthorize("hasRole('ADMIN') or principal.toString() == #userId.toString()")
    @PutMapping("/updateUser/v1/{userId}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        log.info("Updating user id={}", userId);
        userService.updateUser(userId,
            body.get("name"),
            body.get("phone"),
            body.get("country"));
        return ResponseEntity.ok(
            Map.of("message", "User updated successfully"));
    }

    // DELETE soft delete
    // Handles DELETE "/deleteUser/v1/{userId}", reading the id from the path and a reason from the body.
    // Performs a soft delete (deactivation, not physical removal) via the service and returns HTTP 200.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUser/v1/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            @RequestBody SuspendRequest request) {
        // Require JSON body with "reason" field. GlobalExceptionHandler will return 400 for missing/malformed JSON.
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Missing 'reason' in request body"));
        }
        String reason = request.getReason();
        log.info("Deactivating user id={} reason={}", userId, reason);
        userService.softDeleteUser(userId, reason);
        return ResponseEntity.ok(
            Map.of("message", "User deactivated successfully"));
    }

    // POST suspend user
    // Handles POST "/suspendUser/v1/{userId}", taking the id from the path and a reason from the body.
    // Suspends the user via the service (the hardcoded 1L is the acting admin/actor id) and returns HTTP 200.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/suspendUser/v1/{userId}")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable Long userId,
            @RequestBody SuspendRequest request) {
        log.info("Suspending user id={} reason={}", userId, request.getReason());
        userService.suspendUser(userId, request.getReason(), 1L);
        return ResponseEntity.ok(
            Map.of("message", "User suspended successfully"));
    }

    // POST activate user
    // Handles POST "/activateUser/v1/{userId}", reading the id from the URL path.
    // Re-activates a suspended/deactivated user via the service and returns HTTP 200 with a message.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/activateUser/v1/{userId}")
    public ResponseEntity<Map<String, String>> activateUser(
            @PathVariable Long userId,
            @RequestBody ActivateRequest request) {
        log.info("Activating user id={}", userId);
        userService.activateUser(userId);
        return ResponseEntity.ok(
            Map.of("message", "User activated successfully"));
    }
}
package com.mediahub.subscriptionPlan.controller;

import com.mediahub.subscriptionPlan.dto.CreateUserRequest;
import com.mediahub.subscriptionPlan.dto.UpdateUserRequest;
import com.mediahub.subscriptionPlan.model.User;
import com.mediahub.subscriptionPlan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mediaHub/subscriptionPlan/users")


@Tag(
        name = "Users",
        description = "APIs for managing users"
)

public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Create User")
    @PreAuthorize("hasAuthority('user:manage')")
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userService.createUser(request));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Missing or invalid fields"));
        }
    }


    @Operation(summary = "Get All Users")
    @PreAuthorize("hasAuthority('user:manage')")
    @GetMapping("/fetchUsers")
    public ResponseEntity<?> fetchUsers() {
        try {
            List<User> users = userService.fetchUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No users found"));
        }
    }

    @Operation(summary = "Get User By ID")
    @PreAuthorize("hasAuthority('user:manage')")
    @GetMapping("/fetchUser/{userId}")
    public ResponseEntity<?> fetchUserById(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.fetchUserById(userId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
    }

    @Operation(summary = "Search User By Email")
    @PreAuthorize("hasAuthority('user:manage')")
    @GetMapping("/searchByEmail")
    public ResponseEntity<?> searchByEmail(
            @RequestParam String email) {

        try {

            return ResponseEntity.ok(
                    userService.searchByEmail(email)
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @Operation(summary = "Update User")
    @PreAuthorize("hasAuthority('user:manage')")
    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                         @RequestBody UpdateUserRequest request) {
        try {
            return ResponseEntity.ok(userService.updateUser(userId, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
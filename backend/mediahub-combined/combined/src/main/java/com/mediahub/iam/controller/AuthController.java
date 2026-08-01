package com.mediahub.iam.controller;

import com.mediahub.iam.dto.LoginRequest;
import com.mediahub.iam.dto.RegisterRequest;
import com.mediahub.iam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// REST controller handling authentication flows (register, login, logout, token refresh).
// All routes are rooted at "/mediaHub/iam/auth" and return JSON responses.
@RestController
@RequestMapping("/mediaHub/iam/auth")
public class AuthController {

    // Service dependency that contains the actual authentication/credential logic.
    // Spring injects the AuthService bean automatically at startup via field injection.
    @Autowired
    private AuthService authService;

    // POST /auth/register/v1.0
    // Accepts a RegisterRequest body and delegates new-account creation to the service.
    // On success returns HTTP 201 (Created) with a confirmation message.
    @PostMapping("/register/v1.0")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegisterRequest request) {

        authService.register(
            request.getName(),
            request.getEmail(),
            request.getPassword(),
            request.getPhone(),
            request.getCountry()
        );

        return ResponseEntity.status(201).body(
            Map.of("message", "Account created successfully")
        );
    }

    // POST /auth/login/v1.0
    // Takes a LoginRequest (email + password) and asks the service to authenticate the user.
    // Returns HTTP 200 with the full result map, which includes the issued JWT token.
    @PostMapping("/login/v1.0")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request) {

        // ── Return full response including JWT token ───────────────────────────
        Map<String, Object> result = authService.login(
            request.getEmail(),
            request.getPassword()
        );

        return ResponseEntity.ok(result);
    }

    // POST /auth/logout/v1.0
    // Reads the userId from a request parameter and tells the service to end that user's session.
    // Returns HTTP 200 with a message confirming the session was terminated.
    @PostMapping("/logout/v1.0")
    public ResponseEntity<Map<String, String>> logout(
            @RequestParam Long userId) {

        authService.logout(userId);
        return ResponseEntity.ok(
            Map.of("message", "Logged out. Session terminated.")
        );
    }

    // POST /auth/refreshToken/v1.0
    // Reads the userId from a request parameter and requests a fresh token from the service.
    // Returns HTTP 200 with the result map containing the newly generated token.
    @PostMapping("/refreshToken/v1.0")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestParam Long userId) {

        Map<String, Object> result = authService.refreshToken(userId);
        return ResponseEntity.ok(result);
    }
}
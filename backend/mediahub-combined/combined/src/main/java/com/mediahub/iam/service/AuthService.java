package com.mediahub.iam.service;

import com.mediahub.iam.entity.Role;
import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.UserStatus;
import com.mediahub.iam.repository.RoleRepository;
import com.mediahub.iam.repository.UserRepository;
import com.mediahub.iam.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RoleService roleService;

    // ── POST /auth/register/v1.0 ──────────────────────────────────────────────
    public User register(String name, String email,
                         String password, String phone, String country) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        Role role = roleRepository.findByRoleType("subscriber")
            .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setPhone(phone);
        user.setCountry(country);
        user.setRole(role);
        user.setStatus(UserStatus.active);
        user.setIsRevoked(false);

        return userRepository.save(user);
    }

    // ── POST /auth/login/v1.0 ─────────────────────────────────────────────────
    public Map<String, Object> login(String email, String password) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("INVALID_CREDENTIALS"));

        if (user.getStatus() == UserStatus.suspended) {
            throw new RuntimeException("ACCOUNT_SUSPENDED");
        }
        if (user.getStatus() == UserStatus.inactive) {
            throw new RuntimeException("ACCOUNT_INACTIVE");
        }
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        // ── Real JWT token (include permissions for downstream services)
        java.util.List<String> permissions = roleService.getPermissionsForRole(user.getRole().getRoleId());
        String token = jwtUtil.generateToken(user, permissions);

        user.setTokenHash(token);
        user.setIssuedAt(LocalDateTime.now());
        user.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        user.setIsRevoked(false);
        userRepository.save(user);

        System.out.println(token.length());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("tokenType",   "Bearer");
        response.put("expiresIn",   1800);
        response.put("user", Map.of(
            "userId",   user.getUserId(),
            "name",     user.getName(),
            "email",    user.getEmail(),
            "roleId",   user.getRole().getRoleId(),
            "roleType", user.getRole().getRoleType(),
            "status",   user.getStatus()
        ));
        return response;
    }

    // ── POST /auth/logout/v1.0 ────────────────────────────────────────────────
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        user.setTokenHash(null);
        user.setIssuedAt(null);
        user.setExpiresAt(null);
        user.setIsRevoked(true);
        userRepository.save(user);
    }

    // ── POST /auth/refreshToken/v1.0 ──────────────────────────────────────────
    public Map<String, Object> refreshToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (user.getIsRevoked()) {
            throw new RuntimeException("REFRESH_TOKEN_INVALID");
        }
        if (user.getExpiresAt() != null &&
            user.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("REFRESH_TOKEN_INVALID");
        }

        // ── Real JWT token ────────────────────────────────────────────────────
        java.util.List<String> permissions = roleService.getPermissionsForRole(user.getRole().getRoleId());
        String token = jwtUtil.generateToken(user, permissions);

        user.setTokenHash(token);
        user.setIssuedAt(LocalDateTime.now());
        user.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("tokenType",   "Bearer");
        response.put("expiresIn",   1800);
        return response;
    }
}
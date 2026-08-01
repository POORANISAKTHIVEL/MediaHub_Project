package com.mediahub.iam.service;

import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.UserStatus;
import com.mediahub.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ── GET all users ─────────────────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── GET single user ───────────────────────────────────────────────────────
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    // ── PUT update user ───────────────────────────────────────────────────────
    public User updateUser(Long userId, String name,
                           String phone, String country) {
        User user = getUserById(userId);
        if (name    != null) user.setName(name);
        if (phone   != null) user.setPhone(phone);
        if (country != null) user.setCountry(country);
        return userRepository.save(user);
    }

    // ── DELETE soft delete ────────────────────────────────────────────────────
    public User softDeleteUser(Long userId, String reason) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.inactive);
        user.setIsRevoked(true);
        user.setTokenHash(null);
        user.setIssuedAt(null);
        user.setExpiresAt(null);
        user.setRevokedAt(LocalDateTime.now());
        user.setRevocationReason(reason);
        return userRepository.save(user);
    }

    // ── POST suspend user ─────────────────────────────────────────────────────
    public User suspendUser(Long userId, String reason, Long adminId) {
        User user  = getUserById(userId);
        if (user.getStatus() == UserStatus.suspended) {
            throw new RuntimeException("ALREADY_SUSPENDED");
        }
        if (userId.equals(adminId)) {
            throw new RuntimeException("CANNOT_SUSPEND_SELF");
        }
        User admin = getUserById(adminId);
        user.setStatus(UserStatus.suspended);
        user.setIsRevoked(true);
        user.setTokenHash(null);
        user.setIssuedAt(null);
        user.setExpiresAt(null);
        user.setRevokedAt(LocalDateTime.now());
        user.setRevokedBy(admin);
        user.setRevocationReason(reason);
        return userRepository.save(user);
    }

    // ── POST activate user ────────────────────────────────────────────────────
    public User activateUser(Long userId) {
        User user = getUserById(userId);
        if (user.getStatus() == UserStatus.active) {
            throw new RuntimeException("ALREADY_ACTIVE");
        }
        user.setStatus(UserStatus.active);
        user.setIsRevoked(false);
        user.setTokenHash(null);
        user.setIssuedAt(null);
        user.setExpiresAt(null);
        user.setRevokedAt(null);
        user.setRevokedBy(null);
        user.setRevocationReason(null);
        return userRepository.save(user);
    }
}
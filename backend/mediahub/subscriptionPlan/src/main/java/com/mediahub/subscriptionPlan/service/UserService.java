package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateUserRequest;
import com.mediahub.subscriptionPlan.dto.UpdateUserRequest;
import com.mediahub.subscriptionPlan.model.User;
import com.mediahub.subscriptionPlan.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // POST
    public Map<String, String> createUser(CreateUserRequest request) {

        log.info("Creating user with email: {}", request.getEmail());

        Map<String, String> response = new HashMap<>();

        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {

            log.error("Email already exists: {}", request.getEmail());

            throw new IllegalStateException("Email already exists");
        });

        User user = User.builder()
                .name(request.getName())
                .roles(request.getRoles())
                .email(request.getEmail())
                .phone(request.getPhone())
                .country(request.getCountry())
                .build();

        userRepository.save(user);

        log.info("User created successfully");

        response.put("message", "User created successfully");

        return response;
    }

    // GET all
    public List<User> fetchUsers() {

        log.info("Fetching all users");

        return userRepository.findAll();
    }

    // GET by ID
    public User fetchUserById(Long userId) {

        log.info("Fetching user with id: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> {

                    log.error("User not found with id: {}", userId);

                    return new RuntimeException("User not found");
                });
    }

    // SEARCH BY EMAIL - INDEX PERFORMANCE TEST
    public User searchByEmail(String email) {

        long startTime = System.currentTimeMillis();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found");
                });

        long endTime = System.currentTimeMillis();

        log.info("Search Time : {} ms", (endTime - startTime));

        return user;
    }

    // PUT
    public Map<String, String> updateUser(Long userId,
                                          UpdateUserRequest request) {

        log.info("Updating user with id: {}", userId);

        Map<String, String> response = new HashMap<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {

                    log.error("User not found with id: {}", userId);

                    return new RuntimeException("User not found");
                });

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }

        userRepository.save(user);

        log.info("User updated successfully");

        response.put("message", "User updated successfully");

        return response;
    }
}
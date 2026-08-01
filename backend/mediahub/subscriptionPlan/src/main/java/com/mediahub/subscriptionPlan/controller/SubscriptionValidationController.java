package com.mediahub.subscriptionPlan.controller;


import com.mediahub.subscriptionPlan.repository.UserSubscriptionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mediaHub/subscriptionPlan/validation")
public class SubscriptionValidationController {

    private final UserSubscriptionRepository repository;

    public SubscriptionValidationController(
            UserSubscriptionRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAuthority('subscription:view')")
    @GetMapping("/user/{userId}")
    public Boolean validateUser(
            @PathVariable Long userId) {

        return repository
                .findByUserIdAndStatus(
                        userId,
                        "Active")
                .isPresent();
    }
}
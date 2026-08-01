package com.mediahub.subscriptionPlan.controller;

import com.mediahub.subscriptionPlan.dto.CreateSubscriptionRequest;
import com.mediahub.subscriptionPlan.dto.UpdateSubscriptionRequest;
import com.mediahub.subscriptionPlan.model.UserSubscription;
import com.mediahub.subscriptionPlan.service.UserSubscriptionService;
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
@RequestMapping("/mediaHub/subscriptionPlan/usersubscriptions")

@Tag(
        name = "User Subscriptions",
        description = "APIs for subscription operations"
)

public class UserSubscriptionController {

    @Autowired
    private UserSubscriptionService userSubscriptionService;


    @Operation(summary = "Create Subscription")
    // NOTE: principal here is a String (JWT subject), while request.userId is a Long — a raw
    // `principal == #request.userId` never matches (String never equals Long), so every
    // non-admin user was 403'd trying to subscribe themselves. Compare as strings instead.
    @PreAuthorize("hasAuthority('subscription:manage') or principal.toString() == #request.userId.toString()")
    @PostMapping("/createSubscription")
    public ResponseEntity<?> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userSubscriptionService.createSubscription(request));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Missing or invalid fields"));
        }
    }

    @Operation(summary = "Get All Subscriptions")
    @PreAuthorize("hasAuthority('subscription:manage') or hasAuthority('subscription:view')")
    @GetMapping("/fetchSubscriptions")
    public ResponseEntity<?> fetchSubscriptions() {
        try {
            List<UserSubscription> list = userSubscriptionService.fetchSubscriptions();
            return ResponseEntity.ok(list);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No subscriptions found"));
        }
    }

    @Operation(summary = "Get Subscription By ID")
    @PreAuthorize("hasAuthority('subscription:manage') or @userSubscriptionService.isOwner(#subscriptionId, principal) or hasAuthority('subscription:view')")
    @GetMapping("/fetchSubscription/{subscriptionId}")
    public ResponseEntity<?> fetchSubscriptionById(@PathVariable Long subscriptionId) {
        try {
            return ResponseEntity.ok(
                    userSubscriptionService.fetchSubscriptionById(subscriptionId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Subscription not found"));
        }
    }

    @Operation(summary = "Update Subscription")
    @PreAuthorize("hasAuthority('subscription:manage') or @userSubscriptionService.isOwner(#subscriptionId, principal)")
    @PutMapping("/updateSubscription/{subscriptionId}")
    public ResponseEntity<?> updateSubscription(
            @PathVariable Long subscriptionId,
            @RequestBody UpdateSubscriptionRequest request) {

        try {
            return ResponseEntity.ok(
                    userSubscriptionService.updateSubscription(subscriptionId, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @Operation(summary = "Renew Subscription")
    @PreAuthorize("hasAuthority('subscription:manage') or @userSubscriptionService.isOwner(#subscriptionId, principal)")
    @PutMapping("/renewSubscription/{subscriptionId}")
    public ResponseEntity<?> renewSubscription(
            @PathVariable Long subscriptionId,
            @RequestBody UpdateSubscriptionRequest request) {

        try {
            return ResponseEntity.ok(
                    userSubscriptionService.renewSubscription(subscriptionId, request));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @Operation(summary = "Cancel Subscription")
    @PreAuthorize("hasAuthority('subscription:manage') or @userSubscriptionService.isOwner(#subscriptionId, principal)")
    @PutMapping("/cancelSubscription/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Long subscriptionId) {

        try {
            return ResponseEntity.ok(
                    userSubscriptionService.cancelSubscription(subscriptionId));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ✅ ANALYTICS API
    @PreAuthorize("hasAuthority('report:view')")
    @GetMapping("/analytics")
    public ResponseEntity<?> getSubscriptionAnalytics() {
        try {
            return ResponseEntity.ok(
                    userSubscriptionService.getSubscriptionAnalytics());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to fetch analytics"));
        }
    }

    @PreAuthorize("hasAuthority('subscription:manage') or principal.toString() == #userId.toString() or hasAuthority('subscription:view')")
    @GetMapping("/validateSubscription/{userId}")
    public ResponseEntity<?> validateSubscription(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userSubscriptionService
                        .validateSubscription(userId));
    }
}
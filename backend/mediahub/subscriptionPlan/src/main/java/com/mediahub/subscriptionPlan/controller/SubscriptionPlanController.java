package com.mediahub.subscriptionPlan.controller;

import com.mediahub.subscriptionPlan.client.AuditClient;
import com.mediahub.subscriptionPlan.dto.CreatePlanRequest;
import com.mediahub.subscriptionPlan.dto.UpdatePlanRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionPlan;
import com.mediahub.subscriptionPlan.service.SubscriptionPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mediaHub/subscriptionPlan/plans")

@Tag(
        name = "Subscription Plans",
        description = "APIs for managing subscription plans"
)
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

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

    @Operation(
            summary = "Create Subscription Plan",
            description = "Creates a new subscription plan"
    )
    @PreAuthorize("hasAuthority('plan:configure')")
    @PostMapping("/createPlan")
    public ResponseEntity<?> createPlan(@RequestBody CreatePlanRequest request, Authentication authentication) {
        try {
            Map<String, String> result = subscriptionPlanService.createPlan(request);
            auditClient.log("PLAN_CREATED", "SUBSCRIPTION", Long.valueOf(authentication.getName()),
                actorRole(authentication), "SubscriptionPlan", request.getName(),
                "Created plan: " + request.getName(), "LOW");
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Missing or invalid fields"));
        }
    }

    @Operation(
            summary = "Get All Subscription Plans",
            description = "Returns all available subscription plans"
    )
    @PreAuthorize("hasAuthority('plan:view')")
    @GetMapping("/fetchPlans")
    public ResponseEntity<?> fetchPlans() {
        try {
            List<SubscriptionPlan> plans = subscriptionPlanService.fetchPlans();
            return ResponseEntity.ok(plans);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No plans found"));
        }
    }

    @Operation(
            summary = "Get Plan By ID",
            description = "Returns a subscription plan using Plan ID"
    )
    @PreAuthorize("hasAuthority('plan:view')")
    @GetMapping("/fetchPlan/{planId}")
    public ResponseEntity<?> fetchPlanById(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(subscriptionPlanService.fetchPlanById(planId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Plan not found"));
        }
    }

    @Operation(
            summary = "Update Subscription Plan",
            description = "Updates an existing subscription plan"
    )
    @PreAuthorize("hasAuthority('plan:configure')")
    @PutMapping("/updatePlan/{planId}")
    public ResponseEntity<?> updatePlan(
            @PathVariable Long planId,
            @RequestBody UpdatePlanRequest request,
            Authentication authentication) {

        try {
            Map<String, String> result = subscriptionPlanService.updatePlan(planId, request);
            auditClient.log("PLAN_UPDATED", "SUBSCRIPTION", Long.valueOf(authentication.getName()),
                actorRole(authentication), "SubscriptionPlan", planId.toString(),
                "Updated plan", "LOW");
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
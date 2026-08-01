package com.mediahub.subscriptionPlan.controller;

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

    @Operation(
            summary = "Create Subscription Plan",
            description = "Creates a new subscription plan"
    )
    @PreAuthorize("hasAuthority('plan:configure')")
    @PostMapping("/createPlan")
    public ResponseEntity<?> createPlan(@RequestBody CreatePlanRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(subscriptionPlanService.createPlan(request));
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
            @RequestBody UpdatePlanRequest request) {

        try {
            return ResponseEntity.ok(
                    subscriptionPlanService.updatePlan(planId, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreatePlanRequest;
import com.mediahub.subscriptionPlan.dto.UpdatePlanRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionPlan;
import com.mediahub.subscriptionPlan.repository.SubscriptionPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SubscriptionPlanService {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    // POST
    public Map<String, String> createPlan(CreatePlanRequest request) {

        log.info("Creating plan with name: {}", request.getName());

        Map<String, String> response = new HashMap<>();

        subscriptionPlanRepository.findByName(request.getName())
                .ifPresent(existing -> {

                    log.error("Plan already exists: {}", request.getName());

                    throw new IllegalStateException("Plan already exists");
                });

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .price(request.getPrice())
                .billingCycle(request.getBillingCycle())
                .contentAccessLevel(request.getContentAccessLevel())
                .maxDevices(request.getMaxDevices())
                .downloadAllowed(request.getDownloadAllowed())
                .build();

        subscriptionPlanRepository.save(plan);

        log.info("Plan created successfully");

        response.put("message", "Plan created successfully");

        return response;
    }

    // GET all
    public List<SubscriptionPlan> fetchPlans() {

        log.info("Fetching all subscription plans");

        return subscriptionPlanRepository.findAll();
    }

    // GET by ID
    public SubscriptionPlan fetchPlanById(Long planId) {

        log.info("Fetching plan with id: {}", planId);

        return subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> {

                    log.error("Plan not found with id: {}", planId);

                    return new RuntimeException("Plan not found");
                });
    }

    // PUT
    public Map<String, String> updatePlan(Long planId,
                                          UpdatePlanRequest request) {

        log.info("Updating plan with id: {}", planId);

        Map<String, String> response = new HashMap<>();

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> {

                    log.error("Plan not found with id: {}", planId);

                    return new RuntimeException("Plan not found");
                });

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }

        if (request.getBillingCycle() != null) {
            plan.setBillingCycle(request.getBillingCycle());
        }

        if (request.getMaxDevices() != null) {
            plan.setMaxDevices(request.getMaxDevices());
        }

        if (request.getDownloadAllowed() != null) {
            plan.setDownloadAllowed(request.getDownloadAllowed());
        }

        subscriptionPlanRepository.save(plan);

        log.info("Plan updated successfully");

        response.put("message", "Plan updated successfully");

        return response;
    }
}
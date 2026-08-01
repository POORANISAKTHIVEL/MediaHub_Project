package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateSubscriptionRequest;
import com.mediahub.subscriptionPlan.dto.UpdateSubscriptionRequest;
import com.mediahub.subscriptionPlan.model.UserSubscription;
import com.mediahub.subscriptionPlan.repository.UserSubscriptionRepository;
import com.mediahub.subscriptionPlan.service.NotificationClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserSubscriptionService {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private NotificationClientService notificationClientService;

    // POST
    public Map<String, String> createSubscription(CreateSubscriptionRequest request) {

        log.info("Creating subscription for user id: {}", request.getUserId());

        Map<String, String> response = new HashMap<>();

        userSubscriptionRepository.findByUserIdAndStatus(request.getUserId(), "Active")
                .ifPresent(existing -> {
                    log.error("Active subscription already exists for user id: {}", request.getUserId());
                    throw new IllegalStateException("Active subscription already exists");
                });

        UserSubscription subscription = UserSubscription.builder()
                .userId(request.getUserId())
                .planId(request.getPlanId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .renewalType(request.getRenewalType())
                .build();

        userSubscriptionRepository.save(subscription);

        notificationClientService.sendNotification(
                subscription.getUserId(),
                "Subscription created successfully");

        log.info("Subscription created successfully");

        response.put(
                "message",
                "Subscription created successfully");

        return response;
    }

    // GET all
    public List<UserSubscription> fetchSubscriptions() {

        log.info("Fetching all subscriptions");

        return userSubscriptionRepository.findAll();
    }

    // GET by ID
    public UserSubscription fetchSubscriptionById(Long subscriptionId) {

        log.info("Fetching subscription with id: {}", subscriptionId);

        return userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with id: {}", subscriptionId);
                    return new RuntimeException("Subscription not found");
                });
    }

    // PUT - update
    public Map<String, String> updateSubscription(Long subscriptionId,
                                                  UpdateSubscriptionRequest request) {

        log.info("Updating subscription with id: {}", subscriptionId);

        Map<String, String> response = new HashMap<>();

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with id: {}", subscriptionId);
                    return new RuntimeException("Subscription not found");
                });

        Long oldPlanId = subscription.getPlanId();

        if (request.getPlanId() != null) {

            subscription.setPlanId(
                    request.getPlanId());

            notificationClientService.sendNotification(
                    subscription.getUserId(),
                    "Subscription plan changed from "
                            + oldPlanId
                            + " to "
                            + request.getPlanId());
        }

        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }

        if (request.getRenewalType() != null) {
            subscription.setRenewalType(request.getRenewalType());
        }

        userSubscriptionRepository.save(subscription);

        log.info("Subscription updated successfully");

        response.put("message", "Subscription updated successfully");

        return response;
    }

    // PUT - renew
    public Map<String, String> renewSubscription(Long subscriptionId,
                                                 UpdateSubscriptionRequest request) {

        log.info("Renewing subscription with id: {}", subscriptionId);

        Map<String, String> response = new HashMap<>();

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with id: {}", subscriptionId);
                    return new RuntimeException("Subscription not found");
                });

        if (!subscription.getStatus().equals("Active")) {

            log.error("Subscription not eligible for renewal");

            throw new IllegalStateException("Subscription not eligible for renewal");
        }

        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }

        if (request.getRenewalType() != null) {
            subscription.setRenewalType(request.getRenewalType());
        }

        userSubscriptionRepository.save(subscription);

        notificationClientService.sendNotification(
                subscription.getUserId(),
                "Subscription renewed successfully");

        log.info("Subscription renewed successfully");

        response.put(
                "message",
                "Subscription renewed successfully");

        return response;
    }

    // PUT - cancel
    public Map<String, String> cancelSubscription(Long subscriptionId) {

        log.info("Cancelling subscription with id: {}", subscriptionId);

        Map<String, String> response = new HashMap<>();

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with id: {}", subscriptionId);
                    return new RuntimeException("Subscription not found");
                });

        if (subscription.getStatus().equals("Cancelled")) {

            log.error("Subscription already cancelled");

            throw new IllegalStateException("Subscription already cancelled");
        }

        subscription.setStatus("Cancelled");

        userSubscriptionRepository.save(subscription);

        notificationClientService.sendNotification(
                subscription.getUserId(),
                "Subscription cancelled successfully");

        log.info("Subscription cancelled successfully");

        response.put(
                "message",
                "Subscription cancelled successfully");

        return response;
    }

    // ✅ ANALYTICS
    public Map<String, Object> getSubscriptionAnalytics() {

        log.info("Fetching subscription analytics");

        List<UserSubscription> subscriptions =
                userSubscriptionRepository.findAll();

        int totalSubscriptions = subscriptions.size();

        long activeSubscriptions = subscriptions.stream()
                .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
                .count();

        long cancelledSubscriptions = subscriptions.stream()
                .filter(s -> "Cancelled".equalsIgnoreCase(s.getStatus()))
                .count();

        long expiredSubscriptions = subscriptions.stream()
                .filter(s -> "Expired".equalsIgnoreCase(s.getStatus()))
                .count();

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Subscription analytics retrieved successfully");
        response.put("totalSubscriptions", totalSubscriptions);
        response.put("activeSubscriptions", activeSubscriptions);
        response.put("cancelledSubscriptions", cancelledSubscriptions);
        response.put("expiredSubscriptions", expiredSubscriptions);

        log.info("Subscription analytics generated successfully");

        return response;
    }

    // ✅ VALIDATE SUBSCRIPTION FOR CONTENT ACCESS
    public Map<String, Object> validateSubscription(
            Long userId) {

        log.info(
                "Validating subscription for user id: {}",
                userId);

        Map<String, Object> response =
                new HashMap<>();

        boolean activeSubscription =
                userSubscriptionRepository
                        .findByUserIdAndStatus(
                                userId,
                                "Active")
                        .isPresent();

        response.put(
                "userId",
                userId);

        response.put(
                "subscriptionActive",
                activeSubscription);

        return response;
    }

        // Returns true if the given principal (userId) owns the subscription with the provided id.
        public boolean isOwner(Long subscriptionId, Object principal) {
                if (subscriptionId == null || principal == null) return false;
                Long userId;
                if (principal instanceof Long) userId = (Long) principal;
                else {
                        try { userId = Long.valueOf(principal.toString()); }
                        catch (Exception e) { return false; }
                }
                return userSubscriptionRepository.findById(subscriptionId)
                                .map(s -> s.getUserId().equals(userId))
                                .orElse(false);
        }

}
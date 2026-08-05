package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateSubscriptionRequest;
import com.mediahub.subscriptionPlan.dto.UpdateSubscriptionRequest;
import com.mediahub.subscriptionPlan.model.UserSubscription;
import com.mediahub.subscriptionPlan.repository.SubscriptionPlanRepository;
import com.mediahub.subscriptionPlan.repository.UserSubscriptionRepository;
import com.mediahub.subscriptionPlan.service.NotificationClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserSubscriptionService {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private NotificationClientService notificationClientService;

    @Autowired
    private SubscriptionHistoryService subscriptionHistoryService;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    /** In-memory, per-subscription "last reminded" dates so the expiry/upgrade nudge fires at most
     *  once a day no matter how many times fetchSubscriptions() is hit (there's no scheduler here —
     *  this piggybacks on the existing authenticated fetch call instead of needing a service-account
     *  token for a background cron). Resets on restart; acceptable since it only guards spam. */
    private final Map<Long, LocalDate> lastExpiryReminder = new ConcurrentHashMap<>();
    private final Map<Long, LocalDate> lastUpgradeReminder = new ConcurrentHashMap<>();

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

        subscriptionHistoryService.logChange(
                subscription.getSubscriptionId(), subscription.getUserId(),
                null, subscription.getPlanId(), "New", "New subscription created");

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

        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();
        checkExpiryAndUpgradeReminders(subscriptions);
        return subscriptions;
    }

    private void checkExpiryAndUpgradeReminders(List<UserSubscription> subscriptions) {
        LocalDate today = LocalDate.now();
        Double topPrice = subscriptionPlanRepository.findAll().stream()
                .map(p -> p.getPrice() == null ? 0.0 : p.getPrice())
                .max(Double::compareTo).orElse(0.0);

        for (UserSubscription s : subscriptions) {
            if (!"Active".equals(s.getStatus()) || s.getEndDate() == null) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, s.getEndDate());
            if (daysLeft < 0 || daysLeft > 3) continue;

            if (!today.equals(lastExpiryReminder.get(s.getSubscriptionId()))) {
                lastExpiryReminder.put(s.getSubscriptionId(), today);
                String planName = subscriptionPlanRepository.findById(s.getPlanId())
                        .map(p -> p.getName()).orElse("Your");
                // Kept short — the notification service caps message length at 100 chars.
                notificationClientService.sendNotification(
                        s.getUserId(),
                        planName + " plan expires " + s.getEndDate() + " — renew to keep access.");
            }

            double planPrice = subscriptionPlanRepository.findById(s.getPlanId())
                    .map(p -> p.getPrice() == null ? 0.0 : p.getPrice()).orElse(0.0);
            if (planPrice < topPrice && !today.equals(lastUpgradeReminder.get(s.getSubscriptionId()))) {
                lastUpgradeReminder.put(s.getSubscriptionId(), today);
                notificationClientService.sendNotification(
                        s.getUserId(),
                        "Upgrade your plan for more devices, downloads and content access.");
            }
        }
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

        if (request.getPlanId() != null && !request.getPlanId().equals(oldPlanId)) {
            String changeType = "Upgrade";
            try {
                double oldPrice = subscriptionPlanRepository.findById(oldPlanId).map(p -> p.getPrice()).orElse(0.0);
                double newPrice = subscriptionPlanRepository.findById(request.getPlanId()).map(p -> p.getPrice()).orElse(0.0);
                changeType = newPrice < oldPrice ? "Downgrade" : "Upgrade";
            } catch (Exception ignored) { }
            subscriptionHistoryService.logChange(
                    subscription.getSubscriptionId(), subscription.getUserId(),
                    oldPlanId, subscription.getPlanId(), changeType, "Plan changed via update");
        }

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

        subscriptionHistoryService.logChange(
                subscription.getSubscriptionId(), subscription.getUserId(),
                subscription.getPlanId(), subscription.getPlanId(), "Renewal", "Subscription renewed");

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

        subscriptionHistoryService.logChange(
                subscription.getSubscriptionId(), subscription.getUserId(),
                subscription.getPlanId(), subscription.getPlanId(), "Cancellation", "Subscription cancelled");

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
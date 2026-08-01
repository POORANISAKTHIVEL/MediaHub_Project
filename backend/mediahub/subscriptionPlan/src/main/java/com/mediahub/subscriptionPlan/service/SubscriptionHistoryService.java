package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateHistoryRequest;
import com.mediahub.subscriptionPlan.dto.UpdateHistoryRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import com.mediahub.subscriptionPlan.repository.SubscriptionHistoryRepository;
import com.mediahub.subscriptionPlan.service.NotificationClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SubscriptionHistoryService {

    @Autowired
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Autowired
    private NotificationClientService notificationClientService;

    // POST
    public Map<String, String> createHistory(CreateHistoryRequest request) {

        log.info("Creating history for subscription id: {}", request.getSubscriptionId());

        Map<String, String> response = new HashMap<>();

        subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(
                        request.getSubscriptionId(),
                        request.getChangeType())
                .ifPresent(existing -> {

                    log.error("History already exists for subscription id: {}",
                            request.getSubscriptionId());

                    throw new IllegalStateException("History already exists");
                });

        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscriptionId(request.getSubscriptionId())
                .userId(request.getUserId())
                .fromPlanId(request.getFromPlanId())
                .toPlanId(request.getToPlanId())
                .changeType(request.getChangeType())
                .changeDate(request.getChangeDate())
                .remarks(request.getRemarks())
                .build();

        subscriptionHistoryRepository.save(history);

        notificationClientService.sendNotification(
                history.getUserId(),
                "Subscription "
                        + history.getChangeType()
                        + " completed");

        log.info("History created successfully");

        response.put(
                "message",
                "History created successfully");

        return response;
    }

    // GET all
    public List<SubscriptionHistory> fetchHistories() {

        log.info("Fetching all subscription histories");

        return subscriptionHistoryRepository.findAll();
    }

    // GET by ID
    public SubscriptionHistory fetchHistoryById(Long historyId) {

        log.info("Fetching history with id: {}", historyId);

        return subscriptionHistoryRepository.findById(historyId)
                .orElseThrow(() -> {

                    log.error("History not found with id: {}", historyId);

                    return new RuntimeException("History not found");
                });
    }

    // PUT
    public Map<String, String> updateHistory(Long historyId,
                                             UpdateHistoryRequest request) {

        log.info("Updating history with id: {}", historyId);

        Map<String, String> response = new HashMap<>();

        SubscriptionHistory history = subscriptionHistoryRepository.findById(historyId)
                .orElseThrow(() -> {

                    log.error("History not found with id: {}", historyId);

                    return new RuntimeException("History not found");
                });

        if (request.getRemarks() != null) {
            history.setRemarks(request.getRemarks());
        }

        if (request.getChangeDate() != null) {
            history.setChangeDate(request.getChangeDate());
        }

        subscriptionHistoryRepository.save(history);

        log.info("History updated successfully");

        response.put("message", "History updated successfully");

        return response;
    }
}
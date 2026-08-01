package com.mediahub.subscriptionPlan.controller;

import com.mediahub.subscriptionPlan.dto.CreateHistoryRequest;
import com.mediahub.subscriptionPlan.dto.UpdateHistoryRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import com.mediahub.subscriptionPlan.service.SubscriptionHistoryService;
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
@RequestMapping("/mediaHub/subscriptionPlan/subscriptionhistory")

@Tag(
        name = "Subscription History",
        description = "APIs for subscription history management"
)

public class SubscriptionHistoryController {

    @Autowired
    private SubscriptionHistoryService subscriptionHistoryService;

    @Operation(summary = "Create History Record")
    @PreAuthorize("hasAuthority('plan:configure')")
    @PostMapping("/createHistory")
    public ResponseEntity<?> createHistory(@RequestBody CreateHistoryRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(subscriptionHistoryService.createHistory(request));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Missing or invalid fields"));
        }
    }

    @Operation(summary = "Get All History Records")
    @PreAuthorize("hasAuthority('plan:configure')")
    @GetMapping("/fetchHistories")
    public ResponseEntity<?> fetchHistories() {
        try {
            List<SubscriptionHistory> list = subscriptionHistoryService.fetchHistories();
            return ResponseEntity.ok(list);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No history found"));
        }
    }

    @Operation(summary = "Get History By ID")
    @PreAuthorize("hasAuthority('plan:configure')")
    @GetMapping("/fetchHistory/{historyId}")
    public ResponseEntity<?> fetchHistoryById(@PathVariable Long historyId) {
        try {
            return ResponseEntity.ok(subscriptionHistoryService.fetchHistoryById(historyId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "History not found"));
        }
    }

    @Operation(summary = "Update History Record")
    @PreAuthorize("hasAuthority('plan:configure')")
    @PutMapping("/updateHistory/{historyId}")
    public ResponseEntity<?> updateHistory(@PathVariable Long historyId,
                                            @RequestBody UpdateHistoryRequest request) {
        try {
            return ResponseEntity.ok(subscriptionHistoryService.updateHistory(historyId, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
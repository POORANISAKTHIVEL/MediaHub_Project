package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyPayout;
import com.mediahub.royalty.service.RoyaltyPayoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/royalty-payouts")
public class RoyaltyPayoutController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyPayoutController.class);
    private final RoyaltyPayoutService service;

    public RoyaltyPayoutController(RoyaltyPayoutService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RoyaltyPayout> createPayout(
            @RequestBody RoyaltyPayout payout) {
        logger.info("POST /api/royalty-payouts - Create payout request for StatementID: {}", payout.getStatementID());
        RoyaltyPayout created = service.createPayout(payout);
        logger.info("Payout created successfully with ID: {}", created.getPayoutID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RoyaltyPayout>> getAllPayouts() {
        logger.info("GET /api/royalty-payouts - Fetch all payouts");
        List<RoyaltyPayout> payouts = service.getAllPayouts();
        logger.info("Retrieved {} payouts", payouts.size());
        return ResponseEntity.ok(payouts);
    }

    @GetMapping("/{payoutID}")
    public ResponseEntity<RoyaltyPayout> getPayoutById(
            @PathVariable int payoutID) {
        logger.info("GET /api/royalty-payouts/{} - Fetch payout by ID", payoutID);
        RoyaltyPayout payout = service.getPayoutById(payoutID);
        logger.info("Payout with ID: {} retrieved", payoutID);
        return ResponseEntity.ok(payout);
    }

    @PutMapping("/{payoutID}/process")
    public ResponseEntity<Map<String, Object>> processPayout(
            @PathVariable int payoutID) {
        logger.info("PUT /api/royalty-payouts/{}/process - Process payout", payoutID);
        Map<String, Object> response = service.processPayout(payoutID);
        logger.info("Payout with ID: {} processed", payoutID);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{payoutID}/fail")
    public ResponseEntity<Map<String, Object>> failPayout(
            @PathVariable int payoutID,
            @RequestParam String reason) {
        logger.info("PUT /api/royalty-payouts/{}/fail - Fail payout with reason: {}", payoutID, reason);
        Map<String, Object> response = service.failPayout(payoutID, reason);
        logger.info("Payout with ID: {} marked as failed", payoutID);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{payoutID}")
    public ResponseEntity<Map<String, Object>> deletePayout(
            @PathVariable int payoutID) {
        logger.info("DELETE /api/royalty-payouts/{} - Delete payout", payoutID);
        Map<String, Object> response = service.deletePayout(payoutID);
        logger.info("Payout with ID: {} deleted", payoutID);
        return ResponseEntity.ok(response);
    }
}

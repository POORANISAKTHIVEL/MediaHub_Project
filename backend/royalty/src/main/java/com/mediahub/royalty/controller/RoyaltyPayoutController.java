package com.mediahub.royalty.controller;

import com.mediahub.royalty.client.AuditClient;
import com.mediahub.royalty.model.RoyaltyPayout;
import com.mediahub.royalty.service.RoyaltyPayoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/royalty-payouts")
public class RoyaltyPayoutController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyPayoutController.class);
    private final RoyaltyPayoutService service;

    @Autowired
    private AuditClient auditClient;

    public RoyaltyPayoutController(RoyaltyPayoutService service) {
        this.service = service;
    }

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PostMapping
    public ResponseEntity<RoyaltyPayout> createPayout(
            @RequestBody RoyaltyPayout payout,
            Authentication authentication) {
        logger.info("POST /api/royalty-payouts - Create payout request for StatementID: {}", payout.getStatementID());
        RoyaltyPayout created = service.createPayout(payout, Long.valueOf(authentication.getName()));
        logger.info("Payout created successfully with ID: {}", created.getPayoutID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyAuthority('royalty:view','royalty:approve')")
    @GetMapping
    public ResponseEntity<List<RoyaltyPayout>> getAllPayouts() {
        logger.info("GET /api/royalty-payouts - Fetch all payouts");
        List<RoyaltyPayout> payouts = service.getAllPayouts();
        logger.info("Retrieved {} payouts", payouts.size());
        return ResponseEntity.ok(payouts);
    }

    @PreAuthorize("hasAnyAuthority('royalty:view','royalty:approve')")
    @GetMapping("/{payoutID}")
    public ResponseEntity<RoyaltyPayout> getPayoutById(
            @PathVariable int payoutID) {
        logger.info("GET /api/royalty-payouts/{} - Fetch payout by ID", payoutID);
        RoyaltyPayout payout = service.getPayoutById(payoutID);
        logger.info("Payout with ID: {} retrieved", payoutID);
        return ResponseEntity.ok(payout);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PutMapping("/{payoutID}/process")
    public ResponseEntity<Map<String, Object>> processPayout(
            @PathVariable int payoutID,
            Authentication authentication) {
        logger.info("PUT /api/royalty-payouts/{}/process - Process payout", payoutID);
        Map<String, Object> response = service.processPayout(payoutID, Long.valueOf(authentication.getName()));
        logger.info("Payout with ID: {} processed", payoutID);
        auditClient.log("PAYOUT_PROCESSED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyPayout", String.valueOf(payoutID), "Processed royalty payout", "MEDIUM");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PutMapping("/{payoutID}/fail")
    public ResponseEntity<Map<String, Object>> failPayout(
            @PathVariable int payoutID,
            @RequestParam String reason,
            Authentication authentication) {
        logger.info("PUT /api/royalty-payouts/{}/fail - Fail payout with reason: {}", payoutID, reason);
        Map<String, Object> response = service.failPayout(payoutID, reason, Long.valueOf(authentication.getName()));
        logger.info("Payout with ID: {} marked as failed", payoutID);
        auditClient.log("PAYOUT_FAILED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyPayout", String.valueOf(payoutID),
            "Marked payout as failed: " + reason, "MEDIUM");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @DeleteMapping("/{payoutID}")
    public ResponseEntity<Map<String, Object>> deletePayout(
            @PathVariable int payoutID) {
        logger.info("DELETE /api/royalty-payouts/{} - Delete payout", payoutID);
        Map<String, Object> response = service.deletePayout(payoutID);
        logger.info("Payout with ID: {} deleted", payoutID);
        return ResponseEntity.ok(response);
    }
}

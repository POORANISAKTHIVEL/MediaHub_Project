package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyRule;
import com.mediahub.royalty.service.RoyaltyRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/royalty-rules")
public class RoyaltyRuleController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyRuleController.class);
    private final RoyaltyRuleService service;

    public RoyaltyRuleController(RoyaltyRuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RoyaltyRule> createRule(
            @RequestBody RoyaltyRule rule) {
        logger.info("POST /api/royalty-rules - Create rule for CreatorTier: {}", rule.getCreatorTier());
        RoyaltyRule created = service.createRule(rule);
        logger.info("Rule created successfully with ID: {}", created.getRuleID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RoyaltyRule>> getAllRules() {
        logger.info("GET /api/royalty-rules - Fetch all rules");
        List<RoyaltyRule> rules = service.getAllRules();
        logger.info("Retrieved {} rules", rules.size());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{ruleID}")
    public ResponseEntity<RoyaltyRule> getRuleById(
            @PathVariable int ruleID) {
        logger.info("GET /api/royalty-rules/{} - Fetch rule by ID", ruleID);
        RoyaltyRule rule = service.getRuleById(ruleID);
        logger.info("Rule with ID: {} retrieved", ruleID);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{ruleID}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateRule(
            @PathVariable int ruleID) {
        logger.info("PUT /api/royalty-rules/{}/deactivate - Deactivate rule", ruleID);
        Map<String, Object> response = service.deactivateRule(ruleID);
        logger.info("Rule with ID: {} deactivated", ruleID);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ruleID}")
    public ResponseEntity<Map<String, Object>> deleteRule(
            @PathVariable int ruleID) {
        logger.info("DELETE /api/royalty-rules/{} - Delete rule", ruleID);
        Map<String, Object> response = service.deleteRule(ruleID);
        logger.info("Rule with ID: {} deleted", ruleID);
        return ResponseEntity.ok(response);
    }
}

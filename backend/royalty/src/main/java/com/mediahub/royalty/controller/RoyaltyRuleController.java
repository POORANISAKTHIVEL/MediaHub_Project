package com.mediahub.royalty.controller;

import com.mediahub.royalty.client.AuditClient;
import com.mediahub.royalty.model.RoyaltyRule;
import com.mediahub.royalty.service.RoyaltyRuleService;
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
@RequestMapping("/api/royalty-rules")
public class RoyaltyRuleController {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyRuleController.class);
    private final RoyaltyRuleService service;

    @Autowired
    private AuditClient auditClient;

    public RoyaltyRuleController(RoyaltyRuleService service) {
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
    public ResponseEntity<RoyaltyRule> createRule(
            @RequestBody RoyaltyRule rule,
            Authentication authentication) {
        logger.info("POST /api/royalty-rules - Create rule for CreatorTier: {}", rule.getCreatorTier());
        RoyaltyRule created = service.createRule(rule);
        logger.info("Rule created successfully with ID: {}", created.getRuleID());
        auditClient.log("ROYALTY_RULE_CREATED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyRule", String.valueOf(created.getRuleID()),
            "Created royalty rule for tier: " + rule.getCreatorTier(), "LOW");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAuthority('royalty:view')")
    @GetMapping
    public ResponseEntity<List<RoyaltyRule>> getAllRules() {
        logger.info("GET /api/royalty-rules - Fetch all rules");
        List<RoyaltyRule> rules = service.getAllRules();
        logger.info("Retrieved {} rules", rules.size());
        return ResponseEntity.ok(rules);
    }

    @PreAuthorize("hasAuthority('royalty:view')")
    @GetMapping("/{ruleID}")
    public ResponseEntity<RoyaltyRule> getRuleById(
            @PathVariable int ruleID) {
        logger.info("GET /api/royalty-rules/{} - Fetch rule by ID", ruleID);
        RoyaltyRule rule = service.getRuleById(ruleID);
        logger.info("Rule with ID: {} retrieved", ruleID);
        return ResponseEntity.ok(rule);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @PutMapping("/{ruleID}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateRule(
            @PathVariable int ruleID,
            Authentication authentication) {
        logger.info("PUT /api/royalty-rules/{}/deactivate - Deactivate rule", ruleID);
        Map<String, Object> response = service.deactivateRule(ruleID);
        logger.info("Rule with ID: {} deactivated", ruleID);
        auditClient.log("ROYALTY_RULE_DEACTIVATED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyRule", String.valueOf(ruleID), "Deactivated royalty rule", "LOW");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('royalty:approve')")
    @DeleteMapping("/{ruleID}")
    public ResponseEntity<Map<String, Object>> deleteRule(
            @PathVariable int ruleID,
            Authentication authentication) {
        logger.info("DELETE /api/royalty-rules/{} - Delete rule", ruleID);
        Map<String, Object> response = service.deleteRule(ruleID);
        logger.info("Rule with ID: {} deleted", ruleID);
        auditClient.log("ROYALTY_RULE_DELETED", "ROYALTY", Long.valueOf(authentication.getName()),
            actorRole(authentication), "RoyaltyRule", String.valueOf(ruleID), "Deleted royalty rule", "MEDIUM");
        return ResponseEntity.ok(response);
    }
}

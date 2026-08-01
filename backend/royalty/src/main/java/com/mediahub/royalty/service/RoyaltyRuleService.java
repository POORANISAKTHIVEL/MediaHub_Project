package com.mediahub.royalty.service;

import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.model.RoyaltyRule;
import com.mediahub.royalty.repository.RoyaltyRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoyaltyRuleService {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyRuleService.class);
    private final RoyaltyRuleRepository repository;

    public RoyaltyRuleService(RoyaltyRuleRepository repository) {
        this.repository = repository;
    }

    public RoyaltyRule createRule(RoyaltyRule rule) {
        logger.debug("Creating new royalty rule for CreatorTier: {}", rule.getCreatorTier());
        try {
            if (rule.getCreatorTier() == null || rule.getCreatorTier().isEmpty()) {
                logger.warn("Validation failed: CreatorTier is required");
                throw new BadRequestException("CreatorTier is required");
            }
            if (rule.getRevenueSharePercent() <= 0
                    || rule.getRevenueSharePercent() > 100) {
                logger.warn("Validation failed: RevenueSharePercent {} is invalid", rule.getRevenueSharePercent());
                throw new BadRequestException(
                        "RevenueSharePercent must be between 1 and 100");
            }
            String freq = rule.getPayoutFrequency();
            if (freq == null || (!freq.equalsIgnoreCase("Monthly")
                    && !freq.equalsIgnoreCase("Quarterly"))) {
                logger.warn("Validation failed: Invalid PayoutFrequency: {}", freq);
                throw new BadRequestException(
                        "PayoutFrequency must be Monthly or Quarterly");
            }
            if (rule.getEffectiveDate() == null) {
                logger.warn("Validation failed: EffectiveDate is required");
                throw new BadRequestException("EffectiveDate is required");
            }
            rule.setStatus("Active");
            repository.save(rule);
            logger.info("Royalty rule created successfully with ID: {}", rule.getRuleID());
            return rule;
        } catch (Exception e) {
            logger.error("Error creating royalty rule: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<RoyaltyRule> getAllRules() {
        logger.debug("Fetching all royalty rules");
        List<RoyaltyRule> rules = repository.findAll();
        logger.info("Retrieved {} royalty rules", rules.size());
        return rules;
    }

    public RoyaltyRule getRuleById(int ruleID) {
        logger.debug("Fetching royalty rule with ID: {}", ruleID);
        RoyaltyRule rule = repository.findById(ruleID);
        logger.info("Royalty rule with ID: {} fetched successfully", ruleID);
        return rule;
    }

    public Map<String, Object> deactivateRule(int ruleID) {
        logger.debug("Deactivating royalty rule with ID: {}", ruleID);
        try {
            int result = repository.updateStatus(ruleID, "Inactive");
            if (result == 0) {
                logger.error("Royalty rule not found with ID: {}", ruleID);
                throw new ResourceNotFoundException(
                        "Royalty rule not found with ID: " + ruleID);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("ruleID", ruleID);
            response.put("status", "Inactive");
            response.put("statusCode", 200);
            response.put("message", "Royalty rule deactivated successfully.");
            logger.info("Royalty rule with ID: {} deactivated successfully", ruleID);
            return response;
        } catch (Exception e) {
            logger.error("Error deactivating royalty rule with ID: {}: {}", ruleID, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> deleteRule(int ruleID) {
        logger.debug("Deleting royalty rule with ID: {}", ruleID);
        try {
            String status = repository.findStatusById(ruleID);
            if (status.equals("Active")) {
                logger.warn("Cannot delete Active royalty rule with ID: {}", ruleID);
                throw new BadRequestException(
                        "Cannot delete Active royalty rule. Deactivate it first.");
            }
            int result = repository.delete(ruleID);
            if (result == 0) {
                logger.error("Royalty rule not found with ID: {}", ruleID);
                throw new ResourceNotFoundException(
                        "Royalty rule not found with ID: " + ruleID);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("ruleID", ruleID);
            response.put("statusCode", 200);
            response.put("message", "Royalty rule deleted successfully.");
            logger.info("Royalty rule with ID: {} deleted successfully", ruleID);
            return response;
        } catch (Exception e) {
            logger.error("Error deleting royalty rule with ID: {}: {}", ruleID, e.getMessage(), e);
            throw e;
        }
    }
}

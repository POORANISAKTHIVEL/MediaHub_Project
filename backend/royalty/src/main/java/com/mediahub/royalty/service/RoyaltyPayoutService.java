package com.mediahub.royalty.service;

import com.mediahub.royalty.client.CreatorClient;
import com.mediahub.royalty.client.NotificationClient;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.model.RoyaltyPayout;
import com.mediahub.royalty.repository.RoyaltyPayoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoyaltyPayoutService {

    private static final Logger logger = LoggerFactory.getLogger(RoyaltyPayoutService.class);
    private final RoyaltyPayoutRepository repository;

    @Autowired
    private CreatorClient creatorClient;
    @Autowired
    private NotificationClient notificationClient;

    public RoyaltyPayoutService(RoyaltyPayoutRepository repository) {
        this.repository = repository;
    }

    // Payouts had no notifications wired at all — every create/process/fail was silent.
    private void notifyCreatorAndActor(int creatorID, Long actorUserId, String creatorMessage, String actorMessage) {
        Long linkedUserId = creatorClient.getUserId(creatorID);
        if (linkedUserId != null) {
            notificationClient.sendRoyaltyNotification(linkedUserId, creatorMessage, null);
        }
        if (actorUserId != null && !actorUserId.equals(linkedUserId)) {
            notificationClient.sendRoyaltyNotification(actorUserId, actorMessage, null);
        }
    }

    public RoyaltyPayout createPayout(RoyaltyPayout payout, Long actorUserId) {
        logger.debug("Creating new payout with StatementID: {} and CreatorID: {}", payout.getStatementID(), payout.getCreatorID());
        try {
            if (payout.getStatementID() == 0) {
                logger.warn("Validation failed: StatementID is required");
                throw new BadRequestException("StatementID is required");
            }
            if (payout.getCreatorID() == 0) {
                logger.warn("Validation failed: CreatorID is required");
                throw new BadRequestException("CreatorID is required");
            }
            if (payout.getAmount() <= 0) {
                logger.warn("Validation failed: Amount {} is not greater than zero", payout.getAmount());
                throw new BadRequestException("Amount must be greater than zero");
            }
            String method = payout.getMethod();
            if (method == null || (!method.equals("BankTransfer")
                    && !method.equals("WalletCredit"))) {
                logger.warn("Validation failed: Invalid payment method: {}", method);
                throw new BadRequestException(
                        "Method must be BankTransfer or WalletCredit");
            }
            if (payout.getPayoutDate() == null) {
                payout.setPayoutDate(new Date());
            }
            payout.setStatus("Pending");
            repository.save(payout);

            notifyCreatorAndActor(payout.getCreatorID(), actorUserId,
                    "A payout of $" + payout.getAmount() + " has been created for you (Payout ID: " + payout.getPayoutID() + ").",
                    "You created payout " + payout.getPayoutID() + " for $" + payout.getAmount() + ".");

            logger.info("Payout created successfully with ID: {}", payout.getPayoutID());
            return payout;
        } catch (Exception e) {
            logger.error("Error creating payout: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<RoyaltyPayout> getAllPayouts() {
        logger.debug("Fetching all payouts");
        List<RoyaltyPayout> payouts = repository.findAll();
        logger.info("Retrieved {} payouts", payouts.size());
        return payouts;
    }

    public RoyaltyPayout getPayoutById(int payoutID) {
        logger.debug("Fetching payout with ID: {}", payoutID);
        RoyaltyPayout payout = repository.findById(payoutID);
        logger.info("Payout with ID: {} fetched successfully", payoutID);
        return payout;
    }

    public Map<String, Object> processPayout(int payoutID, Long actorUserId) {
        logger.debug("Processing payout with ID: {}", payoutID);
        try {
            RoyaltyPayout payout = repository.findById(payoutID);
            int result = repository.updateStatus(payoutID, "Processed");
            if (result == 0) {
                logger.error("Payout not found with ID: {}", payoutID);
                throw new ResourceNotFoundException(
                        "Payout not found with ID: " + payoutID);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("payoutID", payoutID);
            response.put("status", "Processed");
            response.put("statusCode", 200);
            response.put("message", "Payout processed successfully.");

            if (payout != null) {
                notifyCreatorAndActor(payout.getCreatorID(), actorUserId,
                        "Your payout (ID: " + payoutID + ") has been processed.",
                        "You processed payout " + payoutID + ".");
            }

            logger.info("Payout with ID: {} processed successfully", payoutID);
            return response;
        } catch (Exception e) {
            logger.error("Error processing payout with ID: {}: {}", payoutID, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> failPayout(int payoutID, String reason, Long actorUserId) {
        logger.debug("Failing payout with ID: {} for reason: {}", payoutID, reason);
        try {
            RoyaltyPayout payout = repository.findById(payoutID);
            int result = repository.updateStatus(payoutID, "Failed");
            if (result == 0) {
                logger.error("Payout not found with ID: {}", payoutID);
                throw new ResourceNotFoundException(
                        "Payout not found with ID: " + payoutID);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("payoutID", payoutID);
            response.put("status", "Failed");
            response.put("reason", reason);
            response.put("statusCode", 200);
            response.put("message", "Payout marked as failed.");

            if (payout != null) {
                notifyCreatorAndActor(payout.getCreatorID(), actorUserId,
                        "Your payout (ID: " + payoutID + ") failed: " + reason,
                        "You marked payout " + payoutID + " as failed: " + reason);
            }

            logger.info("Payout with ID: {} marked as failed. Reason: {}", payoutID, reason);
            return response;
        } catch (Exception e) {
            logger.error("Error failing payout with ID: {}: {}", payoutID, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> deletePayout(int payoutID) {
        logger.debug("Deleting payout with ID: {}", payoutID);
        try {
            String status = repository.findStatusById(payoutID);
            if (status.equals("Processed")) {
                logger.warn("Cannot delete Processed payout with ID: {}", payoutID);
                throw new BadRequestException("Cannot delete Processed payout.");
            }
            int result = repository.delete(payoutID);
            if (result == 0) {
                logger.error("Payout not found with ID: {}", payoutID);
                throw new ResourceNotFoundException(
                        "Payout not found with ID: " + payoutID);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("payoutID", payoutID);
            response.put("statusCode", 200);
            response.put("message", "Payout deleted successfully.");
            logger.info("Payout with ID: {} deleted successfully", payoutID);
            return response;
        } catch (Exception e) {
            logger.error("Error deleting payout with ID: {}: {}", payoutID, e.getMessage(), e);
            throw e;
        }
    }
}

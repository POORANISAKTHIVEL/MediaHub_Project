package com.mediahub.contentcatalog.service;

import com.mediahub.contentcatalog.client.NotificationClient;
import com.mediahub.contentcatalog.client.SubscriptionClient;
import com.mediahub.contentcatalog.entity.ContentAsset;
import com.mediahub.contentcatalog.repository.ContentAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentAssetService {

    private static final Logger logger = LoggerFactory.getLogger(ContentAssetService.class);

    @Autowired
    private ContentAssetRepository contentAssetRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private SubscriptionClient subscriptionClient;

    // ✅ CREATE CONTENT
    public String createContent(ContentAsset contentAsset) {

        logger.info("Creating content for creatorId: {}", contentAsset.getCreatorId());

        if (contentAsset.getStatus() == null || contentAsset.getStatus().isEmpty()) {
            contentAsset.setStatus("Draft");
        }

        contentAssetRepository.save(contentAsset);

        logger.info("Content created successfully with title: {}", contentAsset.getTitle());

        return "Content created successfully";
    }

    // ✅ GET ALL
    public List<ContentAsset> getAllContents() {

        logger.info("Fetching all content assets");

        return contentAssetRepository.findAll();
    }

    // ✅ GET BY CREATOR — used by Royalty module
    public List<ContentAsset> getContentsByCreator(int creatorId) {

        logger.info("Fetching content assets for creatorId: {}", creatorId);

        return contentAssetRepository.findByCreatorId(creatorId);
    }

    // ✅ GET BY ID
    public ContentAsset getContentById(int contentId) {

        logger.info("Fetching content with ID: {}", contentId);

        ContentAsset content =
                contentAssetRepository.findById(contentId).orElse(null);

        if (content == null) {

            logger.error("Content not found with ID: {}", contentId);
        }

        return content;
    }

    // ✅ UPDATE CONTENT
    public String updateContent(int contentId,
                                ContentAsset contentAsset) {

        logger.info("Updating content with ID: {}", contentId);

        ContentAsset existing =
                contentAssetRepository.findById(contentId).orElse(null);

        if (existing == null) {

            logger.error("Content not found with ID: {}", contentId);

            return "Content not found";
        }

        existing.setTitle(contentAsset.getTitle());
        existing.setGenre(contentAsset.getGenre());
        existing.setLanguage(contentAsset.getLanguage());
        existing.setSynopsis(contentAsset.getSynopsis());
        existing.setFilePath(contentAsset.getFilePath());
        existing.setThumbnailPath(contentAsset.getThumbnailPath());

        contentAssetRepository.save(existing);

        logger.info("Content updated successfully with ID: {}", contentId);

        return "Content updated successfully";
    }

    // ✅ UPDATE STATUS + NOTIFICATION INTEGRATION
    public String updateContentStatus(int contentId,
                                      String status) {

        logger.info("Updating content status for ID: {} to {}",
                contentId, status);

        ContentAsset existing =
                contentAssetRepository.findById(contentId).orElse(null);

        if (existing == null) {

            logger.error("Content not found with ID: {}", contentId);

            return "Content not found";
        }

        existing.setStatus(status);

        contentAssetRepository.save(existing);

        // ✅ Send Notification when Content is Published
        if ("Published".equalsIgnoreCase(status)) {

            Map<String, Object> notification =
                    new HashMap<>();

            notification.put("userId", 1L);

            notification.put(
                    "message",
                    "New content released: " + existing.getTitle()
            );

            notification.put(
                    "category",
                    "CONTENT"
            );

            notificationClient.sendNotification(notification);

            logger.info(
                    "Notification sent successfully for content: {}",
                    existing.getTitle()
            );
        }

        logger.info(
                "Content status updated successfully for ID: {}",
                contentId
        );

        return "Status updated successfully";
    }

    // ✅ CONTENT ACCESS VALIDATION USING SUBSCRIPTION MODULE
    public Map<String, Object> accessContent(
            Long userId,
            int contentId) {

        logger.info(
                "Validating content access for user: {} and content: {}",
                userId,
                contentId);

        Map<String, Object> response =
                new HashMap<>();

        ContentAsset content =
                contentAssetRepository.findById(contentId)
                        .orElse(null);

        if (content == null) {

            response.put(
                    "message",
                    "Content not found");

            return response;
        }

        Map subscriptionResponse =
                subscriptionClient.validateSubscription(
                        userId);

        Boolean activeSubscription =
                (Boolean) subscriptionResponse.get(
                        "subscriptionActive");

        if (activeSubscription == null
                || !activeSubscription) {

            response.put(
                    "message",
                    "Access denied. Active subscription required");

            return response;
        }

        response.put(
                "message",
                "Access granted");

        response.put(
                "content",
                content);

        logger.info(
                "Access granted for user: {} and content: {}",
                userId,
                contentId);

        return response;
    }

    // ✅ DELETE CONTENT
    public String deleteContent(int contentId) {

        logger.info("Attempting to delete content with ID: {}", contentId);

        ContentAsset existing =
                contentAssetRepository.findById(contentId).orElse(null);

        if (existing == null) {

            logger.error("Content not found with ID: {}", contentId);

            return "Content not found";
        }

        if (!existing.getStatus().equals("Draft")) {

            logger.warn(
                    "Delete failed. Content with ID {} is not in Draft state",
                    contentId
            );

            return "Content can only be deleted when status is Draft";
        }

        contentAssetRepository.deleteById(contentId);

        logger.info(
                "Content deleted successfully with ID: {}",
                contentId
        );

        return "Content deleted successfully";
    }
}
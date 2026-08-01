package com.mediahub.notification.service;
 
import com.mediahub.notification.dto.request.NotificationRequestDTO;
import com.mediahub.notification.dto.response.NotificationResponseDTO;
import com.mediahub.notification.entity.Notification;
import com.mediahub.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
 
@Service
public class NotificationService {
        private static final Logger logger =
        LoggerFactory.getLogger(NotificationService.class);
 
    private final NotificationRepository repository;
 
    public NotificationService(
            NotificationRepository repository) {
        this.repository = repository;
    }

        @Async("notificationExecutor")
        public void notificationAuditLog(Long notificationId,
                                        Long userId) {

        logger.info(
                "Async execution started for notificationId: {}, userId: {}",
                notificationId,
                userId);

        logger.info(
                "Notification audit completed for notificationId: {}",
                notificationId);
        }


        
        // CREATE
        public NotificationResponseDTO createNotification(
                        NotificationRequestDTO request) {

                        logger.info(
                                "Creating notification for userId: {}, category: {}",
                                request.getUserId(),
                                request.getCategory());

                        Notification notification = new Notification();

                        notification.setUserId(request.getUserId());
                        notification.setMessage(request.getMessage());
                        notification.setCategory(request.getCategory());
                        notification.setLicenseId(request.getLicenseId());
                        notification.setContentId(request.getContentId());
                        notification.setExpiryDate(request.getExpiryDate());
                        notification.setStatus(Notification.Status.UNREAD);
                        notification.setCreatedDate(LocalDateTime.now());

                        Notification savedNotification =
                                repository.save(notification);

                        logger.info(
                                "Notification created successfully with id: {}",
                                savedNotification.getNotificationId());

                        notificationAuditLog(
                                savedNotification.getNotificationId(),
                                savedNotification.getUserId());

                        return toResponse(savedNotification);
        }
        
        // GET ALL BY USER ID
        public List<NotificationResponseDTO>
                getAllNotifications(Long userId) {

                logger.info(
                        "Fetching all notifications for userId: {}",
                        userId);

                List<Notification> notifications =
                        repository.findByUserId(userId);

                if (notifications.isEmpty()) {

                        logger.warn(
                                "No notifications found for userId: {}",
                                userId);

                        throw new NoSuchElementException(
                                "No notifications found for userId: "
                                        + userId);
                }

                logger.info(
                        "Retrieved {} notifications for userId: {}",
                        notifications.size(),
                        userId);

                return notifications.stream()
                        .map(this::toResponse)
                        .toList();
        }
        
        // GET UNREAD BY USER ID
        public List<NotificationResponseDTO>
                getUnreadNotifications(Long userId) {

                logger.info(
                        "Fetching unread notifications for userId: {}",
                        userId);

                List<Notification> notifications =
                        repository.findByUserIdAndStatus(
                                userId,
                                Notification.Status.UNREAD);

                if (notifications.isEmpty()) {

                        logger.warn(
                                "No unread notifications found for userId: {}",
                                userId);

                        throw new NoSuchElementException(
                                "No notifications found for userId: "
                                        + userId);
                }

                logger.info(
                        "Retrieved {} unread notifications for userId: {}",
                        notifications.size(),
                        userId);

                return notifications.stream()
                        .map(this::toResponse)
                        .toList();
        }
        
        // UPDATE
        public NotificationResponseDTO updateNotification(
                Long id,
                String status) {

                logger.info(
                        "Updating notification with id: {} to status: {}",
                        id,
                        status);

                Notification notification = repository
                        .findById(id)
                        .orElseThrow(() -> {

                                logger.error(
                                        "Notification not found with id: {}",
                                        id);

                                return new NoSuchElementException(
                                        "Notification not found with id "
                                                + id);
                        });

                if (notification.getStatus()
                        == Notification.Status.DISMISSED) {

                        logger.warn(
                                "Attempt to update dismissed notification id: {}",
                                id);

                        throw new RuntimeException(
                                "Dismissed notification cannot be updated");
                }

                notification.setStatus(
                        Notification.Status.valueOf(
                                status.toUpperCase()));

                Notification updatedNotification =
                        repository.save(notification);

                logger.info(
                        "Notification updated successfully. id: {}, new status: {}",
                        id,
                        updatedNotification.getStatus());

                return toResponse(updatedNotification);
        }
                
        // ==========================
        // ANALYTICS METHOD
        // ==========================
        
        public Map<String, Object> getNotificationAnalytics() {
                logger.info("Fetching notification analytics");

                List<Notification> notifications =
                        repository.findAll();
        
                int totalNotifications =
                        notifications.size();
        
                long unreadNotifications =
                        notifications.stream()
                                .filter(n -> n.getStatus()
                                        == Notification.Status.UNREAD)
                                .count();
        
                long readNotifications =
                        notifications.stream()
                                .filter(n -> n.getStatus()
                                        == Notification.Status.READ)
                                .count();
        
                long dismissedNotifications =
                        notifications.stream()
                                .filter(n -> n.getStatus()
                                        == Notification.Status.DISMISSED)
                                .count();
        
                long contentNotifications =
                        notifications.stream()
                                .filter(n -> n.getCategory()
                                        == Notification.Category.CONTENT)
                                .count();
        
                long subscriptionNotifications =
                        notifications.stream()
                                .filter(n -> n.getCategory()
                                        == Notification.Category.SUBSCRIPTION)
                                .count();
        
                long royaltyNotifications =
                        notifications.stream()
                                .filter(n -> n.getCategory()
                                        == Notification.Category.ROYALTY)
                                .count();
        
                long licenseNotifications =
                        notifications.stream()
                                .filter(n -> n.getCategory()
                                        == Notification.Category.LICENSE)
                                .count();
        
                long editorialNotifications =
                        notifications.stream()
                                .filter(n -> n.getCategory()
                                        == Notification.Category.EDITORIAL)
                                .count();
        
                Map<String, Object> response =
                        new HashMap<>();
        
                response.put(
                        "message",
                        "Notification analytics retrieved successfully");
        
                response.put(
                        "totalNotifications",
                        totalNotifications);
        
                response.put(
                        "unreadNotifications",
                        unreadNotifications);
        
                response.put(
                        "readNotifications",
                        readNotifications);
        
                response.put(
                        "dismissedNotifications",
                        dismissedNotifications);
        
                response.put(
                        "contentNotifications",
                        contentNotifications);
        
                response.put(
                        "subscriptionNotifications",
                        subscriptionNotifications);
        
                response.put(
                        "royaltyNotifications",
                        royaltyNotifications);
        
                response.put(
                        "licenseNotifications",
                        licenseNotifications);
        
                response.put(
                        "editorialNotifications",
                        editorialNotifications);
                
                logger.info(
                "Analytics generated successfully. Total notifications: {}",
                totalNotifications);
        
                return response;
        }
        
        // Convert Entity to ResponseDTO
        private NotificationResponseDTO toResponse(
                Notification notification) {
        
                NotificationResponseDTO dto =
                        new NotificationResponseDTO();
        
                dto.setNotificationId(
                        notification.getNotificationId());
        
                dto.setUserId(
                        notification.getUserId());
        
                dto.setMessage(
                        notification.getMessage());
        
                dto.setCategory(
                        notification.getCategory());

                dto.setLicenseId(
                        notification.getLicenseId());

                dto.setContentId(
                        notification.getContentId());

                dto.setExpiryDate(
                        notification.getExpiryDate());        
        
                dto.setStatus(
                        notification.getStatus());
        
                dto.setCreatedDate(
                        notification.getCreatedDate());
        
                return dto;
        }
}
package com.mediahub.notification.controller;
 
import com.mediahub.notification.dto.request.NotificationRequestDTO;
import com.mediahub.notification.dto.response.NotificationResponseDTO;
import com.mediahub.notification.service.NotificationService;
 
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
 
import java.util.List;
import java.util.NoSuchElementException;

@Tag(
    name = "Notification APIs",
    description = "Notification Management APIs"
)

@RestController
@RequestMapping("/mediaHub/notifications")
public class NotificationController {
   private static final Logger logger =
        LoggerFactory.getLogger(NotificationController.class);     
 
    private final NotificationService service;
 
    public NotificationController(
            NotificationService service) {
        this.service = service;
    }
 
    // POST — Create notification
    // Always triggered as a side effect of some other already-authorized action (a review
    // decision, a license being created, a subscription changing, etc.) in another service —
    // never a standalone privileged action a user takes directly — so any authenticated caller
    // may record one, same as AuditClient's logEvent.
    @Operation(summary = "Create Notification")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createNotification/v1.0")
    public ResponseEntity<?> createNotification(
        @Valid @RequestBody
        NotificationRequestDTO request) {

        logger.info(
                "Create Notification API called for userId: {}",
                request.getUserId());

        service.createNotification(request);

        logger.info(
                "Notification created successfully for userId: {}",
                request.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("{\"message\": \"Notification created successfully\"}");
}
 
    // GET — All notifications for a user
    // Every user can see their own notifications; broader authorities cover admin/support access.
    @Operation(summary = "Get All Notifications")
    @PreAuthorize("hasAnyAuthority('notification:view','notification:read') or principal.toString() == #userId.toString()")
    @GetMapping("/getAllNotifications/v1.0/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>>
        getAllNotifications(
                @PathVariable Long userId) {

        logger.info(
                "Get All Notifications API called for userId: {}",
                userId);

        return ResponseEntity.ok(
                service.getAllNotifications(userId));
     }
 
    // GET — Unread notifications for a user
    @Operation(summary = "Get Unread Notifications")
    @PreAuthorize("hasAnyAuthority('notification:view','notification:read') or principal.toString() == #userId.toString()")
    @GetMapping("/getUnreadNotifications/v1.0/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>>
    getUnreadNotifications(
            @PathVariable Long userId) {

        logger.info(
                "Get Unread Notifications API called for userId: {}",
                userId);

        return ResponseEntity.ok(
                service.getUnreadNotifications(userId));
    }
 
    // PUT — Update notification status
    // A user can mark/dismiss their own notification; notification:update covers admin/support.
    @Operation(summary = "Update Notification Status")
    @PreAuthorize("hasAuthority('notification:update') or @notificationService.isOwner(#id, principal)")
    @PutMapping("/updateNotification/v1.0/{id}")
    public ResponseEntity<?> updateNotification(
            @PathVariable Long id,
            @RequestParam String status) {

        logger.info(
                "Update Notification API called. id: {}, status: {}",
                id,
                status);

        service.updateNotification(id, status);

        logger.info(
                "Notification updated successfully. id: {}",
                id);

        return ResponseEntity.ok(
                "{\"message\": \"Notification updated successfully\"}");
    }
 
    // ==========================
    // ANALYTICS API
    // ==========================
    @PreAuthorize("hasAuthority('notification:analytics')")
    @GetMapping("/analytics/v1.0")
    public ResponseEntity<?> getNotificationAnalytics() {

        logger.info(
                "Notification Analytics API called");

        return ResponseEntity.ok(
                service.getNotificationAnalytics());
    }
 
    // Exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleException(
            RuntimeException e) {

        logger.error(
                "Runtime exception occurred: {}",
                e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" +
                        e.getMessage() + "\"}");
    }
 
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(
            NoSuchElementException e) {

        logger.warn(
                "Resource not found: {}",
                e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"Not Found\", \"message\": \"" +
                        e.getMessage() + "\"}");
    }
}
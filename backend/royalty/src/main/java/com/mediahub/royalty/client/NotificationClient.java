package com.mediahub.royalty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for calling the Notification module.
 * Sends ROYALTY category notifications to a creator after
 * a royalty statement is successfully generated.
 */
@Service
public class NotificationClient {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${notification.service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    /**
     * Fires a ROYALTY notification to the given userId.
     *
     * @param userId    the creator's user ID (Long)
     * @param message   the notification message text
     * @param contentId optional related content ID (nullable)
     */
    public void sendRoyaltyNotification(long userId,
                                        String message,
                                        Integer contentId) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId",    userId);
            body.put("message",   message);
            body.put("category",  "ROYALTY");
            if (contentId != null) {
                body.put("contentId", contentId);
            }

            logger.info(
                    "Sending ROYALTY notification to userId: {}, message: {}",
                    userId, message);

            String authHeader = currentAuthHeader();
            webClient.post()
                    .uri(notificationServiceUrl
                            + "/mediaHub/notifications/createNotification/v1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        response -> logger.info(
                                "Notification sent successfully for userId: {}",
                                userId),
                        error -> logger.error(
                                "Failed to send notification for userId: {} — {}",
                                userId, error.getMessage())
                    );

        } catch (Exception e) {
            // Non-blocking: notification failure must not break the royalty flow
            logger.error(
                    "Error dispatching ROYALTY notification for userId {}: {}",
                    userId, e.getMessage());
        }
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}

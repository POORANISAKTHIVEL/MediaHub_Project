package com.mediahub.editorial.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${notification.service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    public void sendNotification(
            Long userId,
            String message,
            String category) {

        Map<String, Object> request = new HashMap<>();

        request.put("userId", userId);
        request.put("message", message);
        request.put("category", category);

        // Forward the caller's JWT — the notification service requires an authenticated
        // caller, and without this every cross-service notification silently failed.
        String authHeader = currentAuthHeader();

        // Send notification asynchronously to avoid blocking POST response
        webClient.post()
                .uri(notificationServiceUrl + "/mediaHub/notifications/createNotification/v1.0")
                .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> logger.warn("Failed to send notification: {}", error.getMessage()))
                .subscribe(
                    response -> logger.info("Notification sent successfully"),
                    error -> logger.error("Error sending notification to user {}: {}", userId, error.getMessage())
                );
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}
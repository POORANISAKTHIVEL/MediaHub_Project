package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.notification.NotificationRequestDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NotificationClientService {

    private final WebClient webClient;

    public NotificationClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendNotification(
            Long userId,
            String message) {

        NotificationRequestDTO dto =
                new NotificationRequestDTO();

        dto.setUserId(userId);
        dto.setMessage(message);
        dto.setCategory("SUBSCRIPTION");

        String authHeader = currentAuthHeader();
        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}
package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.notification.NotificationRequestDTO;
import org.springframework.stereotype.Service;
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

        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
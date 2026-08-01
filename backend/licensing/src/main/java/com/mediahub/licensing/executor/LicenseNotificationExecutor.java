package com.mediahub.licensing.executor;

import com.mediahub.licensing.dto.request.NotificationRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LicenseNotificationExecutor {
    @Autowired
    private WebClient webClient;
    @Async
    public void sendNotification(
            NotificationRequestDTO notification) {

        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}

package com.mediahub.licensing.executor;

import com.mediahub.licensing.dto.request.NotificationRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LicenseNotificationExecutor {
    @Autowired
    private WebClient webClient;

    // @Async runs on a separate thread, so the caller's Authorization header can't be read
    // via RequestContextHolder here — it has to be captured on the request thread and passed in.
    @Async
    public void sendNotification(
            NotificationRequestDTO notification,
            String authHeader) {

        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}

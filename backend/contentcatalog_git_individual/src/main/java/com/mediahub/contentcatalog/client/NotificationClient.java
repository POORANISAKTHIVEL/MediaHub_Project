package com.mediahub.contentcatalog.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class NotificationClient {

    @Autowired
    private WebClient webClient;

    public void sendNotification(Map<String, Object> request) {

        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
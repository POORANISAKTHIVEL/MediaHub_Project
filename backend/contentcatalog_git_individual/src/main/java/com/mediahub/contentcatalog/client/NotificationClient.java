package com.mediahub.contentcatalog.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class NotificationClient {

    @Autowired
    private WebClient webClient;

    public void sendNotification(Map<String, Object> request) {

        String authHeader = currentAuthHeader();
        webClient.post()
                .uri("http://localhost:8085/mediaHub/notifications/createNotification/v1.0")
                .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION) : null;
    }
}
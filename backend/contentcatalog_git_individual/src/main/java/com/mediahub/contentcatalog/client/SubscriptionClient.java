package com.mediahub.contentcatalog.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SubscriptionClient {

    @Autowired
    private WebClient webClient;

    public Map validateSubscription(Long userId) {

        return webClient.get()
                .uri("http://localhost:8086/mediaHub/subscriptionPlan/usersubscriptions/validateSubscription/"
                        + userId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}

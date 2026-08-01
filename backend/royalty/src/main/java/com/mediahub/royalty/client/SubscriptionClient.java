package com.mediahub.royalty.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SubscriptionClient {

    @Autowired
    private WebClient webClient;

    public Boolean validateSubscription(Long userId) {

        return webClient.get()
                .uri("http://localhost:8086/mediaHub/subscriptionPlan/validation/user/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
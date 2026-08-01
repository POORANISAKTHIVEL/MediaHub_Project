package com.mediahub.royalty.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CreatorClient {

    @Autowired
    private WebClient webClient;

    public Boolean validateCreator(int creatorId) {

        return webClient.get()
                .uri("http://localhost:8093/mediahub/contentCatalog/creator/validateCreator/"
                        + creatorId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
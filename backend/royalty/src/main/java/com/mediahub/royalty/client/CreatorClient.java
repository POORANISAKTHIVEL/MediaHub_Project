package com.mediahub.royalty.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

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

    // Resolves the actual account (User) id linked to a creator profile, so subscription
    // validation checks the right person instead of coincidentally treating the creatorId
    // as if it were a userId.
    public Long getUserId(int creatorId) {

        Map<?, ?> creator = webClient.get()
                .uri("http://localhost:8093/mediahub/contentCatalog/creator/fetchCreatorById/" + creatorId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Object userId = creator != null ? creator.get("userId") : null;
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
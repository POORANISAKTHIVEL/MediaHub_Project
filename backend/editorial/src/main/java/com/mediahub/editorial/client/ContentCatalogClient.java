package com.mediahub.editorial.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ContentCatalogClient {

    @Autowired
    private WebClient webClient;

    public void publishContent(int contentId) {

        Map<String, String> request =
                new HashMap<>();

        request.put("status", "Published");

        // Forward the caller's JWT so Content Catalog can authorize this
        // inter-service call (updateContentStatus requires 'content:write').
        String authHeader = currentAuthHeader();

        webClient.put()
                .uri("http://localhost:8093/mediahub/contentCatalog/contentAsset/updateContentStatus/"
                        + contentId)
                .headers(h -> {
                    if (authHeader != null) {
                        h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    }
                })
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // Pulls the Authorization header from the current incoming request so it
    // can be propagated to downstream services. Returns null when there is no
    // request context (e.g. background/async execution).
    private String currentAuthHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
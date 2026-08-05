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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContentCatalogClient.class);

    @Autowired
    private WebClient webClient;

    // Resolves a content asset's creator to their actual account (User) id, so review
    // outcome notifications can be sent to the person who submitted the content instead of
    // the reviewer who acted on it. Returns null (rather than throwing) on any lookup failure
    // so a notification-service hiccup never blocks the approve/reject/revise action itself.
    @SuppressWarnings("unchecked")
    public Long getCreatorUserId(int contentId) {
        String authHeader = currentAuthHeader();
        try {
            Map<String, Object> content = webClient.get()
                    .uri("http://localhost:8093/mediahub/contentCatalog/contentAsset/fetchContentById/" + contentId)
                    .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            Object creatorId = content != null ? content.get("creatorId") : null;
            if (creatorId == null) return null;

            Map<String, Object> creator = webClient.get()
                    .uri("http://localhost:8093/mediahub/contentCatalog/creator/fetchCreatorById/" + creatorId)
                    .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            Object userId = creator != null ? creator.get("userId") : null;
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            logger.warn("Unable to resolve creator user id for contentId={}: {}", contentId, e.getMessage());
            return null;
        }
    }

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
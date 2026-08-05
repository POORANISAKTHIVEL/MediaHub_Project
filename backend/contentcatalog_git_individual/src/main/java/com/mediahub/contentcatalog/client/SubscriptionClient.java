package com.mediahub.contentcatalog.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscriptionClient {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SubscriptionClient.class);

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

    // Resolves every currently-Active subscriber so a newly-published content asset can notify
    // them it's available, instead of only ever notifying a hardcoded admin userId. Returns an
    // empty list (rather than throwing) on any lookup failure so this never blocks publishing.
    @SuppressWarnings("unchecked")
    public List<Long> getActiveSubscriberUserIds() {
        String authHeader = currentAuthHeader();
        try {
            List<Map<String, Object>> subs = webClient.get()
                    .uri("http://localhost:8086/mediaHub/subscriptionPlan/usersubscriptions/fetchSubscriptions")
                    .headers(h -> { if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader); })
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (subs == null) return Collections.emptyList();
            return subs.stream()
                    .filter(s -> "Active".equals(s.get("status")))
                    .map(s -> Long.valueOf(s.get("userId").toString()))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Unable to resolve active subscribers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}

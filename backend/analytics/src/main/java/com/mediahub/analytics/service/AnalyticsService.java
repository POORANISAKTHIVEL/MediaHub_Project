package com.mediahub.analytics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private static final Logger logger =
            LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private WebClient webClient;

    // ── Service URLs ──────────────────────────────────────────────────────────
    @Value("${contentcatalog.base-url}")
    private String contentCatalogBaseUrl;

    @Value("${subscription.base-url}")
    private String subscriptionBaseUrl;

    @Value("${editorial.base-url}")
    private String editorialBaseUrl;

    @Value("${licensing.base-url}")
    private String licensingBaseUrl;

    @Value("${royalty.base-url}")
    private String royaltyBaseUrl;

    @Value("${notification.base-url}")
    private String notificationBaseUrl;

    @Value("${iam.base-url}")
    private String iamBaseUrl;

    // ── Master Dashboard ─────────────────────────────────────────────────────
    public Map<String, Object> getAnalytics(String authorization) {

        Map<String, Object> dashboard = new HashMap<>();
        WebClient client = authorizedClient(authorization);

        dashboard.put("contentCatalogAnalytics",  getContentCatalogAnalytics(client));
        dashboard.put("subscriptionAnalytics",     getSubscriptionAnalytics(client));
        dashboard.put("editorialAnalytics",        getEditorialAnalytics(client));
        dashboard.put("licensingAnalytics",        getLicensingAnalytics(client));
        dashboard.put("revenueAnalytics",          getRevenueAnalytics(client));
        dashboard.put("notificationAnalytics",     getNotificationAnalytics(client));
        dashboard.put("iamAuditAnalytics",         getIAMAuditAnalytics(client));

        logger.info("Analytics dashboard assembled from 7 modules");
        return dashboard;
    }

    private WebClient authorizedClient(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return webClient;
        }
        return webClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
    }

    // ── 1. Content Catalog ───────────────────────────────────────────────────
    private Map<String, Object> getContentCatalogAnalytics(WebClient client) {

        logger.info("Fetching analytics from Content Catalog Service (port 8093)");

        try {
            List<Map<String, Object>> contents =
                    client.get()
                            .uri(contentCatalogBaseUrl
                                 + "/mediahub/contentCatalog/contentAsset/fetchContents")
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();

            int totalContents = contents != null ? contents.size() : 0;

            Map<String, Integer> statusMap = new HashMap<>();
            Map<String, Integer> typeMap   = new HashMap<>();

            if (contents != null) {
                for (Map<String, Object> c : contents) {
                    String status = String.valueOf(c.getOrDefault("status", "Unknown"));
                    String type   = String.valueOf(c.getOrDefault("type",   "Unknown"));
                    statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
                    typeMap.put(type,     typeMap.getOrDefault(type,     0) + 1);
                }
            }

            List<Map<String, Object>> statusList = buildBreakdownList(statusMap);
            List<Map<String, Object>> typeList   = buildBreakdownList(typeMap);

            Map<String, Object> response = new HashMap<>();
            response.put("totalContents",         totalContents);
            response.put("contentStatusBreakdown", statusList);
            response.put("contentTypeBreakdown",   typeList);
            return response;

        } catch (Exception e) {
            logger.error("Error fetching Content Catalog analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Content Catalog analytics");
        }
    }

    // ── 2. Subscription ──────────────────────────────────────────────────────
    private Map<String, Object> getSubscriptionAnalytics(WebClient client) {

        logger.info("Fetching analytics from Subscription Service (port 8086)");

        try {
            Map response =
                    client.get()
                            .uri(subscriptionBaseUrl
                                 + "/mediaHub/subscriptionPlan/usersubscriptions/analytics")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null) {
                logger.info("Subscription analytics fetched successfully");
                return response;
            }
            return errorResponse("Empty response from Subscription Service");

        } catch (Exception e) {
            logger.error("Error fetching Subscription analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Subscription analytics");
        }
    }

    // ── 3. Editorial ─────────────────────────────────────────────────────────
    private Map<String, Object> getEditorialAnalytics(WebClient client) {

        logger.info("Fetching analytics from Editorial Service (port 9097)");

        try {
            Map response =
                    client.get()
                            .uri(editorialBaseUrl
                                 + "/MediaHub/editorial/analytics")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null) {
                logger.info("Editorial analytics fetched successfully");
                return response;
            }
            return errorResponse("Empty response from Editorial Service");

        } catch (Exception e) {
            logger.error("Error fetching Editorial analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Editorial analytics");
        }
    }

    // ── 4. Licensing ─────────────────────────────────────────────────────────
    private Map<String, Object> getLicensingAnalytics(WebClient client) {

        logger.info("Fetching analytics from Licensing Service (port 8083)");

        try {
            Map response =
                    client.get()
                            .uri(licensingBaseUrl
                                 + "/mediaHub/contentLicensing/analytics/v1.0")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null) {
                logger.info("Licensing analytics fetched successfully");
                return response;
            }
            return errorResponse("Empty response from Licensing Service");

        } catch (Exception e) {
            logger.error("Error fetching Licensing analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Licensing analytics");
        }
    }

    // ── 5. Royalty / Revenue ─────────────────────────────────────────────────
    private Map<String, Object> getRevenueAnalytics(WebClient client) {

        logger.info("Fetching analytics from Royalty Service (port 8045)");

        try {
            Map response =
                    client.get()
                            .uri(royaltyBaseUrl
                                 + "/api/royalty-statements/analytics")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null) {
                logger.info("Royalty/Revenue analytics fetched successfully");
                return response;
            }
            return errorResponse("Empty response from Royalty Service");

        } catch (Exception e) {
            logger.error("Error fetching Royalty analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Revenue analytics");
        }
    }

    // ── 6. Notification ──────────────────────────────────────────────────────
    private Map<String, Object> getNotificationAnalytics(WebClient client) {

        logger.info("Fetching analytics from Notification Service (port 8085)");

        try {
            Map response =
                    client.get()
                            .uri(notificationBaseUrl
                                 + "/mediaHub/notifications/analytics/v1.0")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null) {
                logger.info("Notification analytics fetched successfully");
                return response;
            }
            return errorResponse("Empty response from Notification Service");

        } catch (Exception e) {
            logger.error("Error fetching Notification analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch Notification analytics");
        }
    }

    // ── 7. IAM / Audit Log ───────────────────────────────────────────────────
    private Map<String, Object> getIAMAuditAnalytics(WebClient client) {

        logger.info("Fetching audit analytics from IAM Service (port 8091)");

        try {
            // Fetch all audit events (first 100 for analytics)
            Map eventsResponse =
                    client.get()
                            .uri(iamBaseUrl
                                 + "/mediaHub/auditlog/events/getAllEvents/v1.0?page=0&size=100")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            // Fetch all audit reports
            Map reportsResponse =
                    client.get()
                            .uri(iamBaseUrl
                                 + "/mediaHub/auditlog/reports/getAllReports/v1.0?page=0&size=50")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            Map<String, Object> analytics = new HashMap<>();

            // ── Process audit events ──────────────────────────────────────
            if (eventsResponse != null) {
                Object data = eventsResponse.get("data");
                long totalElements = eventsResponse.containsKey("totalElements")
                        ? Long.parseLong(eventsResponse.get("totalElements").toString())
                        : 0L;

                analytics.put("totalAuditEvents", totalElements);

                // Break down events by moduleSource and severity
                if (data instanceof List) {
                    List<Map<String, Object>> events = (List<Map<String, Object>>) data;

                    Map<String, Integer> moduleMap   = new HashMap<>();
                    Map<String, Integer> severityMap = new HashMap<>();
                    Map<String, Integer> statusMap   = new HashMap<>();

                    for (Map<String, Object> event : events) {
                        String module   = String.valueOf(event.getOrDefault("moduleSource", "UNKNOWN"));
                        String severity = String.valueOf(event.getOrDefault("severity",     "UNKNOWN"));
                        String status   = String.valueOf(event.getOrDefault("status",       "UNKNOWN"));

                        moduleMap.put(module,     moduleMap.getOrDefault(module,     0) + 1);
                        severityMap.put(severity, severityMap.getOrDefault(severity, 0) + 1);
                        statusMap.put(status,     statusMap.getOrDefault(status,     0) + 1);
                    }

                    analytics.put("eventsByModule",   buildBreakdownList(moduleMap));
                    analytics.put("eventsBySeverity", buildBreakdownList(severityMap));
                    analytics.put("eventsByStatus",   buildBreakdownList(statusMap));
                }
            } else {
                analytics.put("totalAuditEvents", 0);
                analytics.put("eventsByModule",   new ArrayList<>());
                analytics.put("eventsBySeverity", new ArrayList<>());
                analytics.put("eventsByStatus",   new ArrayList<>());
            }

            // ── Process audit reports ────────────────────────────────────
            if (reportsResponse != null) {
                long totalReports = reportsResponse.containsKey("totalElements")
                        ? Long.parseLong(reportsResponse.get("totalElements").toString())
                        : 0L;
                analytics.put("totalAuditReports", totalReports);
            } else {
                analytics.put("totalAuditReports", 0);
            }

            analytics.put("message", "IAM Audit analytics retrieved successfully");
            logger.info("IAM Audit analytics fetched successfully");
            return analytics;

        } catch (Exception e) {
            logger.error("Error fetching IAM Audit analytics: {}", e.getMessage());
            return errorResponse("Unable to fetch IAM Audit analytics");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private List<Map<String, Object>> buildBreakdownList(Map<String, Integer> countMap) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : countMap.keySet()) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("label", key);
            obj.put("count", countMap.get(key));
            list.add(obj);
        }
        return list;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}

package com.mediahub.royalty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for calling the Content Catalog module.
 * Fetches all content assets belonging to a creator so the Royalty module
 * can validate editorial approvals per content item.
 */
@Service
public class ContentCatalogClient {

    private static final Logger logger = LoggerFactory.getLogger(ContentCatalogClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${contentcatalog.service.url:http://localhost:8093}")
    private String contentCatalogServiceUrl;

    /**
     * Returns a list of content asset maps for the given creator ID.
     * Each map contains at minimum the contentId field.
     *
     * @param creatorId the creator's ID
     * @return list of content asset data maps
     */
    public List<Map<String, Object>> fetchByCreator(int creatorId) {
        try {
            logger.info("Calling Content Catalog service to fetch content for creatorId: {}", creatorId);
            List<Map<String, Object>> result = webClient.get()
                    .uri(contentCatalogServiceUrl
                            + "/mediahub/contentCatalog/contentAsset/fetchByCreator/"
                            + creatorId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            int count = result != null ? result.size() : 0;
            logger.info("Found {} content assets for creatorId: {}", count, creatorId);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error calling Content Catalog service for creator {}: {}", creatorId, e.getMessage());
            return Collections.emptyList();
        }
    }
}

package com.mediahub.royalty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for calling the Editorial module.
 * Validates whether a content asset has been approved through editorial review
 * before a royalty statement is generated.
 */
@Service
public class EditorialClient {

    private static final Logger logger = LoggerFactory.getLogger(EditorialClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${editorial.service.url:http://localhost:9097}")
    private String editorialServiceUrl;

    /**
     * Returns true if the given contentId has at least one Approved editorial review.
     *
     * @param contentId the content asset's ID
     * @return Boolean flag — true if content is editorially approved
     */
    public Boolean validateApproval(int contentId) {
        try {
            logger.info("Calling Editorial service to validate approval for contentId: {}", contentId);
            Boolean result = webClient.get()
                    .uri(editorialServiceUrl
                            + "/MediaHub/editorial/validateApproval/"
                            + contentId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            logger.info("Content {} approval validation result: {}", contentId, result);
            return result;
        } catch (Exception e) {
            logger.error("Error calling Editorial service for content {}: {}", contentId, e.getMessage());
            return null;
        }
    }
}

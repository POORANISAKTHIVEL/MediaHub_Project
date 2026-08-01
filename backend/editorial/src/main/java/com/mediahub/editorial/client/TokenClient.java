package com.mediahub.editorial.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

// Client for fetching access tokens from the IAM service (port 8091).
// Editorial can use this to obtain JWT tokens for making authenticated requests to other services.
@Service
public class TokenClient {

    private static final Logger logger = LoggerFactory.getLogger(TokenClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${iam.service.url:http://localhost:8091}")
    private String iamServiceUrl;

    /**
     * Fetches an access token from the IAM service for the given email and password.
     * Returns the JWT token if login is successful.
     *
     * @param email User email
     * @param password User password
     * @return Access token (JWT) if login succeeds, null otherwise
     */
    public String getAccessToken(String email, String password) {
        try {
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", email);
            loginRequest.put("password", password);

            // Call IAM login endpoint
            Map response = webClient.post()
                    .uri(iamServiceUrl + "/mediaHub/iam/auth/login/v1.0")
                    .bodyValue(loginRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("accessToken")) {
                logger.info("Successfully obtained access token for user: {}", email);
                return (String) response.get("accessToken");
            }

            logger.warn("Failed to obtain access token for user: {}", email);
            return null;

        } catch (Exception e) {
            logger.error("Error fetching access token from IAM service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Refreshes an existing token (if refresh token endpoint is available).
     * 
     * @param userId User ID
     * @return New access token if refresh succeeds, null otherwise
     */
    public String refreshAccessToken(Long userId) {
        try {
            Map response = webClient.post()
                    .uri(iamServiceUrl + "/mediaHub/iam/auth/refreshToken/v1.0?userId=" + userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("accessToken")) {
                logger.info("Successfully refreshed access token for user: {}", userId);
                return (String) response.get("accessToken");
            }

            logger.warn("Failed to refresh access token for user: {}", userId);
            return null;

        } catch (Exception e) {
            logger.error("Error refreshing access token: {}", e.getMessage());
            return null;
        }
    }
}

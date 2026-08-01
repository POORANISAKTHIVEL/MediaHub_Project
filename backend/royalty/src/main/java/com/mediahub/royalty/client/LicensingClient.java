package com.mediahub.royalty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for calling the Licensing module.
 * Validates whether a creator (licensor) has an active license agreement
 * before a royalty statement can be generated.
 */
@Service
public class LicensingClient {

    private static final Logger logger = LoggerFactory.getLogger(LicensingClient.class);

    @Autowired
    private WebClient webClient;

    @Value("${licensing.service.url:http://localhost:8083}")
    private String licensingServiceUrl;

    /**
     * Returns true if the given licensorId has at least one Active license agreement.
     *
     * @param licensorId the creator's ID used as licensor
     * @return Boolean flag — true if active license exists
     */
    public Boolean validateLicensor(int licensorId) {
        try {
            logger.info("Calling Licensing service to validate licensor ID: {}", licensorId);
            Boolean result = webClient.get()
                    .uri(licensingServiceUrl
                            + "/mediaHub/contentLicensing/validateLicensor/"
                            + licensorId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            logger.info("Licensor {} validation result: {}", licensorId, result);
            return result;
        } catch (Exception e) {
            logger.error("Error calling Licensing service for licensor {}: {}", licensorId, e.getMessage());
            return null;
        }
    }
}

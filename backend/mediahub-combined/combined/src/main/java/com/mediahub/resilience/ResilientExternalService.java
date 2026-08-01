package com.mediahub.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example Resilient Service demonstrating Circuit Breaker usage
 * 
 * The @CircuitBreaker annotation automatically applies circuit breaker protection
 * to the method. When the circuit is OPEN, the fallback method is invoked.
 */
@Service
public class ResilientExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientExternalService.class);

    /**
     * Simulate calling an external API/service with circuit breaker protection
     * 
     * Configured with:
     * - Circuit breaker name: "externalServiceBreaker"
     * - Fallback method: externalServiceFallback
     * - Automatically retries and monitors failure rates
     */
    @CircuitBreaker(name = "externalServiceBreaker", fallbackMethod = "externalServiceFallback")
    public String callExternalService(String request) {
        logger.info("Calling external service with request: {}", request);
        
        // Simulate external API call
        // In real scenarios, this would be an HTTP call or database query
        return "Response from external service: " + request;
    }

    /**
     * Fallback method executed when circuit is OPEN or operation fails
     */
    public String externalServiceFallback(String request, Exception ex) {
        logger.warn("Circuit breaker activated! Using fallback response for request: {}", request);
        logger.error("Original error: {}", ex.getMessage());
        
        // Return default/cached response
        return "Service temporarily unavailable. Fallback response: " + request;
    }

    /**
     * Another example: Database query with circuit breaker
     */
    @CircuitBreaker(name = "externalServiceBreaker", fallbackMethod = "getDataFallback")
    public String fetchDataFromRemote(String id) {
        logger.info("Fetching data from remote service with id: {}", id);
        // Simulated remote call
        return "Data for " + id;
    }

    /**
     * Fallback for data fetch operation
     */
    public String getDataFallback(String id, Exception ex) {
        logger.warn("Database/Remote fetch failed for id: {}. Returning cached data.", id);
        return "Cached data for " + id;
    }
}

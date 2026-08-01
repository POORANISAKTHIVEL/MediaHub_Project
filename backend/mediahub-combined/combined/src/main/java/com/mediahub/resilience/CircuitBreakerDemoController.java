package com.mediahub.resilience;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller demonstrating Circuit Breaker usage
 * 
 * Endpoints to test circuit breaker behavior
 */
@RestController
@RequestMapping("/api/circuit-breaker")
public class CircuitBreakerDemoController {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerDemoController.class);

    @Autowired
    private ResilientExternalService resilientService;

    /**
     * Test endpoint: Call external service with circuit breaker
     * GET /api/circuit-breaker/call?request=test
     */
    @GetMapping("/call")
    public ResponseEntity<String> callService(@RequestParam String request) {
        try {
            String result = resilientService.callExternalService(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            logger.error("Error occurred: ", ex);
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Test endpoint: Fetch data with circuit breaker
     * GET /api/circuit-breaker/fetch?id=123
     */
    @GetMapping("/fetch")
    public ResponseEntity<String> fetchData(@RequestParam String id) {
        try {
            String result = resilientService.fetchDataFromRemote(id);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            logger.error("Error occurred: ", ex);
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Health check endpoint
     * GET /api/circuit-breaker/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Circuit Breaker service is running");
    }
}

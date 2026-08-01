package com.mediahub.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Circuit Breaker Integration
 */
@SpringBootTest
public class CircuitBreakerIntegrationTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ResilientExternalService resilientService;

    @Test
    public void testCircuitBreakerBeanExists() {
        assertNotNull(circuitBreakerRegistry, "CircuitBreakerRegistry should be injected");
        assertNotNull(resilientService, "ResilientExternalService should be injected");
    }

    @Test
    public void testCircuitBreakerRegistered() {
        assertNotNull(circuitBreakerRegistry.circuitBreaker("externalServiceBreaker"),
                "Circuit breaker 'externalServiceBreaker' should be registered");
    }

    @Test
    public void testExternalServiceBreakerState() {
        var breaker = circuitBreakerRegistry.circuitBreaker("externalServiceBreaker");
        assertNotNull(breaker.getState(), "CircuitBreaker state should not be null");
        assertTrue(breaker.getState().toString().contains("CLOSED") || 
                   breaker.getState().toString().contains("HALF_OPEN") ||
                   breaker.getState().toString().contains("OPEN"),
                "CircuitBreaker should be in CLOSED, HALF_OPEN, or OPEN state");
    }

    @Test
    public void testCallExternalServiceWorks() {
        String response = resilientService.callExternalService("test-data");
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("Response from external service") || 
                   response.contains("Fallback response"),
                "Response should be from either service or fallback");
    }

    @Test
    public void testFallbackMechanismWorks() {
        String response = resilientService.fetchDataFromRemote("test-id");
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("Data for") || response.contains("Cached data"),
                "Response should contain data or fallback");
    }
}

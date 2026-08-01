package com.mediahub.notification.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Configuration
public class Resilience4jCircuitBreakerConfig {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jCircuitBreakerConfig.class);

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .minimumNumberOfCalls(10)
                .slowCallRateThreshold(100)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        registry.getEventPublisher()
                .onEntryAdded(event -> logger.info("Circuit Breaker added: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> logger.info("Circuit Breaker removed: {}", event.getRemovedEntry().getName()));
        return registry;
    }

    @Bean
    public CircuitBreaker externalServiceBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("externalServiceBreaker", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .minimumNumberOfCalls(5)
                .build());
    }
}

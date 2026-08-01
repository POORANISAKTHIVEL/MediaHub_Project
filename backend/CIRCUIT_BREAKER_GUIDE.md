## Circuit Breaker Implementation Guide

### Overview
This project now includes **Resilience4j Circuit Breaker** - a fault tolerance library that prevents cascading failures in microservices.

### What is a Circuit Breaker?
The circuit breaker pattern monitors method calls and, when failures reach a threshold, "breaks" (halts) calls to the failing service and returns a fallback response instead.

**States:**
- **CLOSED**: Normal operation, calls pass through
- **OPEN**: Threshold exceeded, calls are blocked (fallback used)
- **HALF_OPEN**: Testing if service recovered, limited calls allowed

---

## Installation & Configuration

### 1. Dependencies Added
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

### 2. Properties Configured
Located in `src/main/resources/application.properties`:
```properties
resilience4j.circuitbreaker.instances.externalServiceBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.externalServiceBreaker.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.externalServiceBreaker.minimum-number-of-calls=5
```

---

## Usage Example

### Basic: Annotate a Method with Circuit Breaker
```java
@Service
public class MyService {
    
    @CircuitBreaker(name = "externalServiceBreaker", fallbackMethod = "fallback")
    public String callExternalAPI(String data) {
        // Your code that might fail
        return externalAPI.fetch(data);
    }
    
    // Fallback method (same signature + Exception parameter)
    public String fallback(String data, Exception ex) {
        return "Service unavailable, using cached data";
    }
}
```

### Using the Injected Service
```java
@RestController
public class MyController {
    
    @Autowired
    private MyService service;
    
    @GetMapping("/data")
    public String getData() {
        return service.callExternalAPI("request");
    }
}
```

---

## Current Implementation

### Files Created:
1. **CircuitBreakerConfig.java** - Configuration class with circuit breaker beans
2. **ResilientExternalService.java** - Example service with @CircuitBreaker methods
3. **CircuitBreakerDemoController.java** - REST endpoints to test circuit breaker

### Test the Circuit Breaker:
```bash
# Normal request
curl http://localhost:8091/api/circuit-breaker/call?request=test

# Fetch data
curl http://localhost:8091/api/circuit-breaker/fetch?id=123

# Health check
curl http://localhost:8091/api/circuit-breaker/health

# View metrics
curl http://localhost:8091/actuator/health
```

---

## Configuration Parameters Explained

| Parameter | Value | Description |
|-----------|-------|-------------|
| `failure-rate-threshold` | 50% | Circuit opens when 50% of calls fail |
| `minimum-number-of-calls` | 5 | Minimum calls before evaluating threshold |
| `wait-duration-in-open-state` | 30s | Time to wait before trying HALF_OPEN |
| `permitted-number-of-calls-in-half-open-state` | 3 | Calls allowed in HALF_OPEN state |
| `slow-call-duration-threshold` | 2s | Calls taking >2s are considered slow |
| `automatic-transition-from-open-to-half-open-enabled` | true | Auto transition to HALF_OPEN |

---

## Adding Circuit Breaker to Other Microservices

### For each microservice:

1. **Add dependencies** to their `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

2. **Copy CircuitBreakerConfig.java** to their `config` package

3. **Add to application.properties**:
```properties
resilience4j.circuitbreaker.instances.externalServiceBreaker.registerHealthIndicator=true
resilience4j.circuitbreaker.instances.externalServiceBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.externalServiceBreaker.wait-duration-in-open-state=30s
```

4. **Annotate your service methods**:
```java
@CircuitBreaker(name = "externalServiceBreaker", fallbackMethod = "fallback")
public String yourMethod() { ... }
```

---

## Monitoring & Observability

### Health Endpoint
`GET /actuator/health`
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "details": {
        "externalServiceBreaker": {
          "status": "SUCCESS",
          "details": {
            "state": "CLOSED",
            "failureRate": "-1.0%"
          }
        }
      }
    }
  }
}
```

### Metrics Endpoint
`GET /actuator/metrics/resilience4j.circuitbreaker.calls`
- View call statistics
- Count of successful, failed, slow calls
- Call duration distribution

---

## Best Practices

1. **Use descriptive circuit breaker names**: `userServiceBreaker`, `paymentServiceBreaker`
2. **Define meaningful fallback responses**: Cache data or default values
3. **Log failures**: Use Logger in fallback methods for debugging
4. **Configure based on SLA**: Adjust thresholds per service critical requirements
5. **Monitor metrics**: Regularly check health and metrics endpoints
6. **Test fallback scenarios**: Verify fallback execution during failures

---

## Advanced: Custom Circuit Breaker Instance

Create additional circuit breakers for different services:

```java
@Bean
public CircuitBreaker paymentServiceBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("paymentServiceBreaker", CircuitBreakerConfig.custom()
            .failureRateThreshold(30)  // More strict for payment service
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .minimumNumberOfCalls(10)
            .build());
}
```

Then use it:
```java
@CircuitBreaker(name = "paymentServiceBreaker", fallbackMethod = "paymentFallback")
public boolean processPayment(Payment p) { ... }
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Circuit always OPEN | Reduce `failure-rate-threshold` or check error logs |
| Fallback not called | Ensure fallback method signature matches |
| Metrics not showing | Enable actuator endpoints: `management.endpoints.web.exposure.include=health,metrics,prometheus` |

---

**For more info**: [Resilience4j Documentation](https://resilience4j.readme.io/docs/getting-started)

## Circuit Breaker - Quick Reference

### ⚡ In 30 Seconds
The circuit breaker protects your app from cascading failures. When a service fails repeatedly, it automatically stops calling it and returns a fallback response instead.

---

### 🔧 Setup (Already Done!)
```
✅ Dependencies added to pom.xml
✅ CircuitBreakerConfig.java created
✅ application.properties configured
✅ Example service & controller created
```

---

### 💻 Use It (Copy-Paste Ready)

**1. Add annotation to any risky method:**
```java
@Service
public class MyService {
    @CircuitBreaker(name = "externalServiceBreaker", fallbackMethod = "fallback")
    public String riskyOperation() {
        return externalAPI.call();  // Can fail
    }

    public String fallback(Exception ex) {
        return "Fallback response";
    }
}
```

**2. Inject and use:**
```java
@Autowired
private MyService service;

String result = service.riskyOperation();  // Auto-protected!
```

---

### 🧪 Test It
```bash
curl http://localhost:8091/api/circuit-breaker/call?request=test
curl http://localhost:8091/actuator/health
```

---

### 📊 Configuration (application.properties)
```properties
# Circuit opens when 50% of 5+ calls fail, stays open 30 seconds
resilience4j.circuitbreaker.instances.externalServiceBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.externalServiceBreaker.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.externalServiceBreaker.wait-duration-in-open-state=30s
```

---

### 📈 Monitor
```
http://localhost:8091/actuator/health      → Circuit state
http://localhost:8091/actuator/metrics     → Stats
```

---

### 🎯 Common Use Cases
- **Database Calls**: Protect from slow/fallen database
- **External APIs**: Prevent cascading failures
- **Microservice Calls**: Protect from dependent service outages
- **Cache Fallback**: Return cached data when main source fails

---

### ⚠️ Fallback Rules
```java
// Method signature must match + add Exception parameter
public String fallback(String param1, int param2, Exception ex) {
    // Return cached/default data
}
```

---

**Full Guide**: See `CIRCUIT_BREAKER_GUIDE.md`

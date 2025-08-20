# 🚀 **How to Use Runtime TTL Management in Spring Boot Applications**

## 🎯 **Your Question Answered**

> **"If this is implemented in a Spring Boot application, I want to enable logs for a class or for a log statement during runtime, how can I do that?"**

## ✅ **Simple Answer: Multiple Ways to Enable Expired Logs**

### **Method 1: Direct Service Injection (Recommended)**

```java
@Service
public class ProductionDebugService {
    
    @Autowired
    private TTLManagementService ttlService;
    
    public void enableExpiredLogs(String reason) {
        // Enable ALL expired logs globally
        ttlService.enableAllTTL(reason);
        
        // Now all your expired logs work!
        logger.debug("This DEBUG log was expired but now works!");
        logger.info("This INFO log was expired but now works!");
    }
    
    public void enableSpecificClassLogs(Class<?> clazz, String reason) {
        // Enable expired logs for specific class only
        TTLOverride override = TTLOverride.bypass();
        ttlService.overrideClassTTL(clazz, override, reason);
        
        // Now logs from this class work even if expired
    }
    
    public void extendTTLForClass(Class<?> clazz, int extraDays, String reason) {
        // Extend TTL by extra days for specific class
        TTLOverride override = TTLOverride.extend(extraDays);
        ttlService.overrideClassTTL(clazz, override, reason);
        
        // Now logs from this class have extended TTL
    }
}
```

### **Method 2: REST API Calls (External Tools/Scripts)**

```bash
# Enable ALL expired logs
curl -X POST http://localhost:8080/api/ttl/enable \
  -H "Content-Type: application/json" \
  -d '{"reason": "Production issue investigation"}'

# Enable expired logs for specific class
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "bypass",
    "reason": "User service debugging"
  }'

# Extend TTL for specific class by 30 days
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "extend",
    "extraDays": 30,
    "reason": "Extended debugging period"
  }'
```

### **Method 3: Command Line Interface**

```bash
# Using Spring Boot Actuator endpoints
curl http://localhost:8080/actuator/ttl/enable?reason=Debugging

# Check current status
curl http://localhost:8080/actuator/ttl/status
```

## 🔧 **Step-by-Step Implementation**

### **Step 1: Add Dependencies**

```xml
<dependencies>
    <!-- Logger TTL Framework -->
    <dependency>
        <groupId>io.github.krishnachaitanyap</groupId>
        <artifactId>logger-ttl</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### **Step 2: Create TTL Management Service**

```java
@Service
public class TTLManagementService {
    
    private final TTLManager ttlManager = TTLManager.getInstance();
    
    public void enableAllTTL(String reason) {
        ttlManager.enableAllTTL();
        // Log the operation
    }
    
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        ttlManager.overrideClassTTL(clazz, override);
        // Log the operation
    }
    
    public void extendGlobalTTL(int extraDays, String reason) {
        ttlManager.setGlobalTTLExtension(extraDays);
        // Log the operation
    }
}
```

### **Step 3: Create REST Controller**

```java
@RestController
@RequestMapping("/api/ttl")
public class TTLManagementController {
    
    @Autowired
    private TTLManagementService ttlService;
    
    @PostMapping("/enable")
    public ResponseEntity<String> enableAllTTL(@RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "REST API call");
        ttlService.enableAllTTL(reason);
        return ResponseEntity.ok("All TTL rules enabled: " + reason);
    }
    
    @PostMapping("/class/{className}/override")
    public ResponseEntity<String> overrideClassTTL(
            @PathVariable String className,
            @RequestBody Map<String, Object> request) {
        
        try {
            Class<?> clazz = Class.forName(className);
            String overrideType = (String) request.get("overrideType");
            String reason = (String) request.getOrDefault("reason", "REST API call");
            
            TTLOverride override = createOverride(overrideType, request);
            ttlService.overrideClassTTL(clazz, override, reason);
            
            return ResponseEntity.ok("Class TTL overridden: " + className);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body("Class not found: " + className);
        }
    }
}
```

## 🎯 **Real-World Use Cases**

### **Use Case 1: Production Issue Investigation**

```java
@Service
public class IncidentResponseService {
    
    @Autowired
    private TTLManagementService ttlService;
    
    public void startInvestigation(String incidentId) {
        // Enable all expired logs for investigation
        ttlService.enableAllTTL("Incident investigation: " + incidentId);
        
        // Now all expired DEBUG and INFO logs work
        logger.info("Investigation started for incident: {}", incidentId);
        logger.debug("Debug logs now enabled for investigation");
        
        // Send notification to team
        notificationService.notifyTeam("TTL overrides enabled for incident: " + incidentId);
    }
    
    public void endInvestigation(String incidentId) {
        // Restore normal TTL behavior
        ttlService.disableAllTTL("Investigation complete: " + incidentId);
        
        logger.info("Investigation ended for incident: {}", incidentId);
        notificationService.notifyTeam("TTL overrides disabled for incident: " + incidentId);
    }
}
```

### **Use Case 2: Compliance Audit**

```java
@Service
public class ComplianceAuditService {
    
    @Autowired
    private TTLManagementService ttlService;
    
    public void startAudit(String auditId, int extraDays) {
        // Extend TTL for audit period
        ttlService.extendGlobalTTL(extraDays, "Compliance audit: " + auditId);
        
        logger.info("Compliance audit started: {} - TTL extended by {} days", auditId, extraDays);
        
        // Schedule automatic cleanup after audit
        scheduleCleanup(auditId, extraDays);
    }
    
    private void scheduleCleanup(String auditId, int extraDays) {
        // Schedule cleanup after audit period
        long cleanupDelay = extraDays * 24 * 60 * 60 * 1000L; // Convert days to milliseconds
        
        new Thread(() -> {
            try {
                Thread.sleep(cleanupDelay);
                ttlService.setGlobalTTLExtension(0);
                logger.info("Compliance audit cleanup completed: {}", auditId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
```

### **Use Case 3: Performance Monitoring**

```java
@Service
public class PerformanceMonitoringService {
    
    @Autowired
    private TTLManagementService ttlService;
    
    public void enablePerformanceLogging(String componentName, long durationMs) {
        try {
            Class<?> componentClass = Class.forName(componentName);
            
            // Override TTL to bypass for performance monitoring
            TTLOverride override = TTLOverride.bypass();
            ttlService.overrideClassTTL(componentClass, override, "Performance monitoring");
            
            logger.info("Performance logging enabled for: {} for {} ms", componentName, durationMs);
            
            // Schedule automatic cleanup
            scheduleCleanup(componentClass, durationMs);
            
        } catch (ClassNotFoundException e) {
            logger.error("Component not found: {}", componentName, e);
        }
    }
    
    private void scheduleCleanup(Class<?> componentClass, long durationMs) {
        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
                ttlService.removeClassTTLOverride(componentClass);
                logger.info("Performance logging disabled for: {}", componentClass.getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
```

## 🚀 **Quick Start Commands**

### **Enable All Expired Logs**
```bash
# Via REST API
curl -X POST http://localhost:8080/api/ttl/enable \
  -d '{"reason": "Production debugging"}'

# Via Actuator (if configured)
curl http://localhost:8080/actuator/ttl/enable?reason=Debugging
```

### **Enable Specific Class Logs**
```bash
# Bypass TTL for UserService
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -d '{"overrideType": "bypass", "reason": "User service debugging"}'

# Extend TTL for PaymentService by 30 days
curl -X POST http://localhost:8080/api/ttl/class/com.example.PaymentService/override \
  -d '{"overrideType": "extend", "extraDays": 30, "reason": "Payment audit"}'
```

### **Check Status**
```bash
# Get current TTL management status
curl http://localhost:8080/api/ttl/status

# Get detailed status via Actuator
curl http://localhost:8080/actuator/ttl/status
```

### **Disable Overrides**
```bash
# Disable all TTL overrides
curl -X POST http://localhost:8080/api/ttl/disable \
  -d '{"reason": "Debugging complete"}'

# Clear all overrides
curl -X DELETE http://localhost:8080/api/ttl/clear?reason=Cleanup
```

## 🔒 **Security Considerations**

### **Spring Security Integration**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/ttl/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

### **Method-Level Security**
```java
@Service
public class SecureTTLService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void enableAllTTL(String reason) {
        // Only admins can enable all TTL
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        // Admins and operators can override specific classes
    }
}
```

## 📊 **Monitoring and Alerts**

### **Health Check**
```java
@Component
public class TTLManagementHealthIndicator implements HealthIndicator {
    
    @Autowired
    private TTLManagementService ttlService;
    
    @Override
    public Health health() {
        TTLManagementStatus status = ttlService.getStatus();
        
        if (status.isGlobalTTLEnabled()) {
            return Health.down()
                .withDetail("warning", "Global TTL override is enabled")
                .withDetail("reason", "This should be temporary")
                .build();
        }
        
        return Health.up()
            .withDetail("activeOverrides", status.getScheduledOverridesCount())
            .build();
    }
}
```

### **Metrics**
```java
@Component
public class TTLManagementMetrics {
    
    @Scheduled(fixedRate = 10000)
    public void recordMetrics() {
        TTLManagementStatus status = ttlService.getStatus();
        
        // Record active overrides
        Gauge.builder("ttl.overrides.active")
            .register(meterRegistry, status.getScheduledOverridesCount());
        
        // Record global TTL status
        Gauge.builder("ttl.global.enabled")
            .register(meterRegistry, status.isGlobalTTLEnabled() ? 1 : 0);
    }
}
```

## 🎉 **Summary**

With the Logger TTL Framework integrated into your Spring Boot application, you can now:

1. **Enable expired logs instantly** via service calls or REST APIs
2. **Override TTL for specific classes** without affecting others
3. **Extend TTL periods** for compliance or debugging needs
4. **Schedule automatic cleanup** of TTL overrides
5. **Monitor TTL management** through Spring Boot Actuator
6. **Secure TTL operations** with Spring Security
7. **Track TTL management** with metrics and health checks

**The key benefit**: You can now dynamically control which logs are active during runtime, making your application much more debuggable and maintainable in production environments!

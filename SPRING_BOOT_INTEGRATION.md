# 🚀 Spring Boot Integration for Runtime TTL Management

## Overview

This guide shows you how to integrate the Logger TTL Framework's Runtime TTL Management with Spring Boot applications. You'll learn how to enable expired logs, override TTL rules, and manage logging behavior dynamically through Spring Boot services and REST APIs.

## 🏗️ **Architecture Components**

### 1. **TTLManagementService** - Spring Service
- **Purpose**: Spring-managed service for TTL operations
- **Features**: Event publishing, scheduled cleanup, monitoring
- **Configuration**: Configurable via application.properties

### 2. **TTLManagementController** - REST API
- **Purpose**: HTTP endpoints for TTL management
- **Features**: CRUD operations, scheduling, status monitoring
- **Security**: Can be secured with Spring Security

### 3. **TTLManagementEvent** - Spring Events
- **Purpose**: Event-driven TTL management
- **Features**: Audit trail, monitoring, integration with other Spring components

## 📦 **Dependencies Required**

Add these to your `pom.xml`:

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
    
    <!-- Spring Boot Starter Actuator (optional, for monitoring) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

## ⚙️ **Configuration**

### application.properties
```properties
# TTL Management Configuration
ttl.management.enabled=true
ttl.management.auto-cleanup=true
ttl.management.cleanup-interval=300000

# Logging Configuration
logging.level.com.logger.ttl=DEBUG
logging.level.com.yourpackage=INFO

# Actuator Endpoints (optional)
management.endpoints.web.exposure.include=health,info,ttl
```

### application.yml
```yaml
ttl:
  management:
    enabled: true
    auto-cleanup: true
    cleanup-interval: 300000

logging:
  level:
    com.logger.ttl: DEBUG
    com.yourpackage: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info,ttl
```

## 🚀 **Usage Examples**

### 1. **Enable All Expired Logs (Production Debugging)**

#### Via Service Injection
```java
@Service
public class ProductionDebugService {
    
    private final TTLManagementService ttlService;
    private final TTLLogger logger = TTLLoggerFactory.getLogger(ProductionDebugService.class);
    
    public ProductionDebugService(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    public void enableDebugMode(String reason) {
        logger.info("🔧 Enabling production debug mode: {}", reason);
        
        // Enable all TTL rules globally
        ttlService.enableAllTTL(reason);
        
        // Log current status
        TTLManagementStatus status = ttlService.getStatus();
        logger.info("TTL Status: {}", status.getOverridesSummary());
        
        // Now all expired logs work
        logger.debug("DEBUG logs enabled for investigation");
        logger.info("INFO logs enabled for investigation");
    }
    
    public void disableDebugMode(String reason) {
        logger.info("🔧 Disabling production debug mode: {}", reason);
        
        // Restore normal TTL behavior
        ttlService.disableAllTTL(reason);
        
        // Log final status
        TTLManagementStatus status = ttlService.getStatus();
        logger.info("TTL Status: {}", status.getOverridesSummary());
    }
}
```

#### Via REST API
```bash
# Enable all TTL rules
curl -X POST http://localhost:8080/api/ttl/enable \
  -H "Content-Type: application/json" \
  -d '{"reason": "Production issue investigation"}'

# Check status
curl http://localhost:8080/api/ttl/status

# Disable all TTL rules
curl -X POST http://localhost:8080/api/ttl/disable \
  -H "Content-Type: application/json" \
  -d '{"reason": "Investigation complete"}'
```

### 2. **Override TTL for Specific Class**

#### Via Service
```java
@Service
public class UserServiceTTLManager {
    
    private final TTLManagementService ttlService;
    private final TTLLogger logger = TTLLoggerFactory.getLogger(UserServiceTTLManager.class);
    
    public UserServiceTTLManager(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    public void enableUserServiceLogs(String reason) {
        logger.info("Enabling TTL override for UserService: {}", reason);
        
        // Override UserService TTL to never expire
        TTLOverride override = TTLOverride.bypass();
        ttlService.overrideClassTTL(UserService.class, override, reason);
        
        logger.info("UserService TTL override applied");
    }
    
    public void extendUserServiceTTL(int extraDays, String reason) {
        logger.info("Extending UserService TTL by {} days: {}", extraDays, reason);
        
        // Extend UserService TTL by specified days
        TTLOverride override = TTLOverride.extend(extraDays);
        ttlService.overrideClassTTL(UserService.class, override, reason);
        
        logger.info("UserService TTL extended by {} days", extraDays);
    }
    
    public void replaceUserServiceTTL(String startDate, int ttlDays, LogLevel[] levels, String reason) {
        logger.info("Replacing UserService TTL configuration: {}", reason);
        
        // Replace UserService TTL with new configuration
        TTLOverride override = TTLOverride.replace(startDate, ttlDays, levels);
        ttlService.overrideClassTTL(UserService.class, override, reason);
        
        logger.info("UserService TTL configuration replaced");
    }
}
```

#### Via REST API
```bash
# Override class TTL to bypass (never expire)
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "bypass",
    "reason": "User service debugging"
  }'

# Extend class TTL by 30 days
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "extend",
    "extraDays": 30,
    "reason": "Extended debugging period"
  }'

# Replace class TTL configuration
curl -X POST http://localhost:8080/api/ttl/class/com.example.UserService/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "replace",
    "startDate": "2025-12-31T23:59:59Z",
    "ttlDays": 365,
    "levels": ["ERROR", "WARN"],
    "reason": "Long-term error logging"
  }'
```

### 3. **Override TTL for Specific Method**

#### Via Service
```java
@Service
public class MethodTTLManager {
    
    private final TTLManagementService ttlService;
    private final TTLLogger logger = TTLLoggerFactory.getLogger(MethodTTLManager.class);
    
    public MethodTTLManager(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    public void enableMethodLogs(Class<?> clazz, String methodName, String reason) {
        logger.info("Enabling TTL override for method {}.{}: {}", 
                   clazz.getSimpleName(), methodName, reason);
        
        // Override method TTL to never expire
        TTLOverride override = TTLOverride.bypass();
        ttlService.overrideMethodTTL(clazz, methodName, override, reason);
        
        logger.info("Method TTL override applied");
    }
    
    public void extendMethodTTL(Class<?> clazz, String methodName, int extraDays, String reason) {
        logger.info("Extending method {}.{} TTL by {} days: {}", 
                   clazz.getSimpleName(), methodName, extraDays, reason);
        
        // Extend method TTL by specified days
        TTLOverride override = TTLOverride.extend(extraDays);
        ttlService.overrideMethodTTL(clazz, methodName, override, reason);
        
        logger.info("Method TTL extended by {} days", extraDays);
    }
}
```

#### Via REST API
```bash
# Override method TTL to bypass
curl -X POST http://localhost:8080/api/ttl/method/com.example.UserService/getUser/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "bypass",
    "reason": "getUser method debugging"
  }'

# Extend method TTL by 15 days
curl -X POST http://localhost:8080/api/ttl/method/com.example.UserService/getUser/override \
  -H "Content-Type: application/json" \
  -d '{
    "overrideType": "extend",
    "extraDays": 15,
    "reason": "Extended getUser debugging"
  }'
```

### 4. **Schedule TTL Overrides (Automatic Cleanup)**

#### Via Service
```java
@Service
public class ScheduledTTLManager {
    
    private final TTLManagementService ttlService;
    private final TTLLogger logger = TTLLoggerFactory.getLogger(ScheduledTTLManager.class);
    
    public ScheduledTTLManager(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    public void scheduleDebugMode(String className, long durationMs, String reason) {
        logger.info("Scheduling debug mode for {} for {} ms: {}", 
                   className, durationMs, reason);
        
        try {
            Class<?> clazz = Class.forName(className);
            TTLOverride override = TTLOverride.bypass();
            
            // Schedule override with automatic removal
            ttlService.scheduleTTLOverride(
                UUID.randomUUID().toString(),
                clazz,
                override,
                durationMs,
                reason
            );
            
            logger.info("Debug mode scheduled for {} ms", durationMs);
            
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", className, e);
        }
    }
    
    public void scheduleTTLExtension(String className, int extraDays, long durationMs, String reason) {
        logger.info("Scheduling TTL extension for {} by {} days for {} ms: {}", 
                   className, extraDays, durationMs, reason);
        
        try {
            Class<?> clazz = Class.forName(className);
            TTLOverride override = TTLOverride.extend(extraDays);
            
            // Schedule override with automatic removal
            ttlService.scheduleTTLOverride(
                UUID.randomUUID().toString(),
                clazz,
                override,
                durationMs,
                reason
            );
            
            logger.info("TTL extension scheduled for {} ms", durationMs);
            
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", className, e);
        }
    }
}
```

#### Via REST API
```bash
# Schedule debug mode for 1 hour
curl -X POST http://localhost:8080/api/ttl/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "className": "com.example.UserService",
    "overrideType": "bypass",
    "durationMs": 3600000,
    "reason": "Temporary debugging session"
  }'

# Schedule TTL extension for 30 minutes
curl -X POST http://localhost:8080/api/ttl/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "className": "com.example.UserService",
    "overrideType": "extend",
    "extraDays": 30,
    "durationMs": 1800000,
    "reason": "Temporary audit period"
  }'
```

### 5. **Event-Driven TTL Management**

```java
@Component
public class TTLManagementEventListener {
    
    private final TTLLogger logger = TTLLoggerFactory.getLogger(TTLManagementEventListener.class);
    
    @EventListener
    public void handleTTLManagementEvent(TTLManagementEvent event) {
        logger.info("TTL Management Event: {} - {}", event.getEventType(), event.getMessage());
        
        // You can add custom logic here:
        // - Send notifications
        // - Update monitoring dashboards
        // - Trigger alerts
        // - Log to external systems
        
        switch (event.getEventType()) {
            case GLOBAL_TTL_ENABLED:
                logger.warn("⚠️ Global TTL override enabled: {}", event.getReason());
                // Send alert to operations team
                break;
                
            case CLASS_TTL_OVERRIDDEN:
                logger.info("📝 Class TTL overridden: {}", event.getMessage());
                // Update monitoring dashboard
                break;
                
            case TTL_OVERRIDE_SCHEDULED:
                logger.info("⏰ TTL override scheduled: {}", event.getMessage());
                // Schedule reminder notification
                break;
                
            case CLEANUP_EXECUTED:
                logger.debug("🧹 TTL cleanup executed: {}", event.getMessage());
                // Update metrics
                break;
        }
    }
}
```

### 6. **Spring Boot Actuator Integration**

```java
@Component
public class TTLManagementEndpoint {
    
    private final TTLManagementService ttlService;
    
    public TTLManagementEndpoint(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    @ReadOperation
    public TTLManagementStatus getStatus() {
        return ttlService.getStatus();
    }
    
    @WriteOperation
    public Map<String, Object> enableAllTTL(@RequestParam String reason) {
        ttlService.enableAllTTL(reason);
        return Map.of(
            "message", "All TTL rules enabled",
            "reason", reason,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    @WriteOperation
    public Map<String, Object> disableAllTTL(@RequestParam String reason) {
        ttlService.disableAllTTL(reason);
        return Map.of(
            "message", "All TTL rules disabled",
            "reason", reason,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    @WriteOperation
    public Map<String, Object> extendGlobalTTL(@RequestParam int extraDays, @RequestParam String reason) {
        ttlService.extendGlobalTTL(extraDays, reason);
        return Map.of(
            "message", "Global TTL extended by " + extraDays + " days",
            "extraDays", extraDays,
            "reason", reason,
            "timestamp", System.currentTimeMillis()
        );
    }
}
```

## 🔒 **Security Considerations**

### 1. **Spring Security Integration**
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

### 2. **Method-Level Security**
```java
@Service
public class SecureTTLManagementService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void enableAllTTL(String reason) {
        // Implementation
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        // Implementation
    }
}
```

## 📊 **Monitoring and Metrics**

### 1. **Micrometer Integration**
```java
@Component
public class TTLManagementMetrics {
    
    private final MeterRegistry meterRegistry;
    private final TTLManagementService ttlService;
    
    public TTLManagementMetrics(MeterRegistry meterRegistry, TTLManagementService ttlService) {
        this.meterRegistry = meterRegistry;
        this.ttlService = ttlService;
    }
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void recordMetrics() {
        TTLManagementStatus status = ttlService.getStatus();
        
        // Record current overrides count
        Gauge.builder("ttl.overrides.active")
            .description("Number of active TTL overrides")
            .register(meterRegistry, status.getScheduledOverridesCount());
        
        // Record total operations
        Gauge.builder("ttl.operations.total")
            .description("Total TTL management operations")
            .register(meterRegistry, status.getTotalOperations());
        
        // Record global TTL status
        Gauge.builder("ttl.global.enabled")
            .description("Global TTL override status")
            .register(meterRegistry, status.isGlobalTTLEnabled() ? 1 : 0);
    }
}
```

### 2. **Health Indicator**
```java
@Component
public class TTLManagementHealthIndicator implements HealthIndicator {
    
    private final TTLManagementService ttlService;
    
    public TTLManagementHealthIndicator(TTLManagementService ttlService) {
        this.ttlService = ttlService;
    }
    
    @Override
    public Health health() {
        try {
            TTLManagementStatus status = ttlService.getStatus();
            
            if (status.isTTLManagementEnabled()) {
                return Health.up()
                    .withDetail("globalTTLEnabled", status.isGlobalTTLEnabled())
                    .withDetail("globalTTLExtension", status.getGlobalTTLExtension())
                    .withDetail("activeOverrides", status.getScheduledOverridesCount())
                    .withDetail("totalOperations", status.getTotalOperations())
                    .build();
            } else {
                return Health.down()
                    .withDetail("reason", "TTL Management is disabled")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🧪 **Testing**

### 1. **Unit Tests**
```java
@ExtendWith(SpringExtension.class)
class TTLManagementServiceTest {
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private TTLManagementService ttlService;
    
    @Test
    void testEnableAllTTL() {
        // Given
        String reason = "Test debugging";
        
        // When
        ttlService.enableAllTTL(reason);
        
        // Then
        TTLManagementStatus status = ttlService.getStatus();
        assertTrue(status.isGlobalTTLEnabled());
        assertEquals(1, status.getTotalOperations());
    }
}
```

### 2. **Integration Tests**
```java
@SpringBootTest
@AutoConfigureTestDatabase
class TTLManagementIntegrationTest {
    
    @Autowired
    private TTLManagementService ttlService;
    
    @Test
    void testCompleteTTLManagementFlow() {
        // Enable all TTL
        ttlService.enableAllTTL("Integration test");
        
        // Verify status
        TTLManagementStatus status = ttlService.getStatus();
        assertTrue(status.isGlobalTTLEnabled());
        
        // Override specific class
        TTLOverride override = TTLOverride.bypass();
        ttlService.overrideClassTTL(String.class, override, "Test override");
        
        // Verify override
        TTLOverride retrieved = ttlService.getClassTTLOverride(String.class);
        assertNotNull(retrieved);
        
        // Clear all overrides
        ttlService.clearAllOverrides("Test cleanup");
        
        // Verify cleanup
        status = ttlService.getStatus();
        assertFalse(status.isGlobalTTLEnabled());
    }
}
```

## 🚀 **Production Deployment**

### 1. **Docker Configuration**
```dockerfile
FROM openjdk:11-jre-slim
COPY target/your-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. **Kubernetes Deployment**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ttl-management-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ttl-management-app
  template:
    metadata:
      labels:
        app: ttl-management-app
    spec:
      containers:
      - name: app
        image: your-registry/ttl-management-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: TTL_MANAGEMENT_ENABLED
          value: "true"
        - name: TTL_MANAGEMENT_AUTO_CLEANUP
          value: "true"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
```

## 📚 **Best Practices**

### 1. **Use Cases**
- **Production Debugging**: Enable expired logs temporarily for investigation
- **Compliance Audits**: Extend TTL for audit periods
- **Performance Monitoring**: Override TTL for specific components during monitoring
- **Emergency Situations**: Quickly enable all logs for critical issues

### 2. **Security**
- **Role-based access control** for TTL management operations
- **Audit logging** for all TTL changes
- **Time-limited overrides** for sensitive operations
- **Monitoring and alerting** for unusual TTL management activity

### 3. **Performance**
- **Use targeted overrides** instead of global overrides when possible
- **Schedule automatic cleanup** for temporary overrides
- **Monitor memory usage** of scheduled overrides
- **Use event-driven architecture** for TTL management operations

### 4. **Monitoring**
- **Track TTL override usage** and patterns
- **Monitor performance impact** of TTL overrides
- **Alert on unusual TTL management activity**
- **Maintain audit trail** of all TTL changes

## 🎉 **Conclusion**

With this Spring Boot integration, you can now:

1. **Manage TTL rules dynamically** through Spring services
2. **Control TTL via REST APIs** for external tools and scripts
3. **Schedule automatic TTL overrides** with cleanup
4. **Monitor TTL management** through Spring Boot Actuator
5. **Integrate with Spring Security** for access control
6. **Use event-driven architecture** for TTL management
7. **Deploy in production** with proper monitoring and security

The Logger TTL Framework is now fully integrated with Spring Boot, providing enterprise-grade runtime TTL management capabilities for your Spring applications!

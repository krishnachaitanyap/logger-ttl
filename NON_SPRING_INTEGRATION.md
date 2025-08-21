# 🚀 **Non-Spring Boot Integration Layer**

The Logger TTL Framework provides a comprehensive **non-Spring Boot integration layer** that allows any Java application to build custom integrations and update TTL configurations during runtime, without requiring Spring Boot dependencies.

## 📋 **Table of Contents**

- [Overview](#overview)
- [Core Components](#core-components)
- [Integration Patterns](#integration-patterns)
- [Event-Driven Architecture](#event-driven-architecture)
- [Custom Integration Examples](#custom-integration-examples)
- [Configuration Properties](#configuration-properties)
- [Best Practices](#best-practices)
- [Migration Guide](#migration-guide)

## 🎯 **Overview**

The non-Spring integration layer provides:

- **Event Publishing System**: Publish TTL management events to custom listeners
- **Integration Manager**: Centralized management of TTL operations and integrations
- **Custom Controllers**: Build business-specific TTL management logic
- **Scheduled Overrides**: Automatically manage TTL overrides with time-based expiration
- **Monitoring Integration**: Integrate with custom monitoring and alerting systems
- **Runtime Configuration**: Update TTL settings during application execution

## 🏗️ **Core Components**

### **1. TTLEventPublisher**
Singleton event publisher that works without Spring Boot.

```java
TTLEventPublisher eventPublisher = TTLEventPublisher.getInstance();

// Add event listeners
eventPublisher.addListener(new CustomTTLListener());
eventPublisher.addListener(event -> System.out.println("Event: " + event.getEventType()));

// Publish events
eventPublisher.publishEvent(TTLEventType.GLOBAL_TTL_ENABLED, 
    "All TTL rules enabled", "Production debugging", null);
```

### **2. TTLIntegrationManager**
Comprehensive integration manager providing high-level TTL management operations.

```java
TTLIntegrationManager integrationManager = TTLIntegrationManager.getInstance();

// Enable all TTL rules
integrationManager.enableAllTTL("Production debugging");

// Override class TTL
TTLOverride bypassOverride = TTLOverride.bypass();
integrationManager.overrideClassTTL(PaymentService.class, bypassOverride, 
    "Customer request");

// Schedule temporary override
integrationManager.scheduleOverride("temp-override-1", UserService.class, 
    TTLOverride.extend(7), 3600000, "Temporary debugging");
```

### **3. TTLEventListener Interface**
Interface for building custom TTL event listeners.

```java
public class CustomTTLListener implements TTLEventListener {
    @Override
    public void onTTLEvent(TTLEvent event) {
        switch (event.getEventType()) {
            case GLOBAL_TTL_ENABLED:
                handleGlobalTTLEnabled(event);
                break;
            case CLASS_TTL_OVERRIDDEN:
                handleClassTTLOverridden(event);
                break;
            // ... handle other event types
        }
    }
    
    private void handleGlobalTTLEnabled(TTLEvent event) {
        // Custom business logic
        System.out.println("Global TTL enabled: " + event.getReason());
    }
}
```

### **4. TTLEvent and TTLEventType**
Event classes and types for TTL management operations.

```java
// Event types available
TTLEventType.GLOBAL_TTL_ENABLED
TTLEventType.GLOBAL_TTL_DISABLED
TTLEventType.CLASS_TTL_OVERRIDDEN
TTLEventType.METHOD_TTL_OVERRIDDEN
TTLEventType.FIELD_TTL_OVERRIDDEN
TTLEventType.TTL_OVERRIDE_SCHEDULED
TTLEventType.TTL_OVERRIDE_REMOVED
TTLEventType.ALL_OVERRIDES_CLEARED
TTLEventType.CUSTOM_EVENT
```

## 🔄 **Integration Patterns**

### **Pattern 1: Event-Driven Integration**
Listen to TTL management events and react accordingly.

```java
public class ProductionMonitoringIntegration {
    
    public ProductionMonitoringIntegration() {
        TTLIntegrationManager integrationManager = TTLIntegrationManager.getInstance();
        
        // Listen to TTL events
        integrationManager.addEventListener(event -> {
            if (event.getEventType() == TTLEventType.GLOBAL_TTL_ENABLED) {
                enableProductionMonitoring();
            } else if (event.getEventType() == TTLEventType.GLOBAL_TTL_DISABLED) {
                disableProductionMonitoring();
            }
        });
    }
    
    private void enableProductionMonitoring() {
        // Custom logic: Enable production monitoring
        System.out.println("Production monitoring enabled due to TTL changes");
    }
    
    private void disableProductionMonitoring() {
        // Custom logic: Disable production monitoring
        System.out.println("Production monitoring disabled due to TTL changes");
    }
}
```

### **Pattern 2: Custom Controller Integration**
Build business-specific TTL management controllers.

```java
public class BusinessTTLController {
    
    private final TTLIntegrationManager integrationManager;
    
    public BusinessTTLController() {
        this.integrationManager = TTLIntegrationManager.getInstance();
    }
    
    public void enableCustomerDebugMode(String customerId, String reason) {
        // Business logic: Enable debug logging for specific customer
        TTLOverride debugOverride = TTLOverride.bypass();
        integrationManager.overrideClassTTL(CustomerService.class, debugOverride, reason);
        
        // Schedule automatic removal after 24 hours
        integrationManager.scheduleOverride("customer-debug-" + customerId, 
            CustomerService.class, debugOverride, 86400000, 
            "Customer debug mode for " + customerId);
    }
    
    public void enableComplianceAudit(String auditId, int durationDays) {
        // Business logic: Enable logging for compliance audit
        TTLOverride auditOverride = TTLOverride.extend(durationDays);
        integrationManager.overrideClassTTL(PaymentService.class, auditOverride, 
            "Compliance audit: " + auditId);
        integrationManager.overrideClassTTL(UserService.class, auditOverride, 
            "Compliance audit: " + auditId);
    }
    
    public void enableProductionTroubleshooting(String issueId) {
        // Business logic: Enable debug logging for production issue
        integrationManager.enableAllTTL("Production troubleshooting: " + issueId);
        integrationManager.extendGlobalTTL(1, "Issue resolution: " + issueId);
    }
}
```

### **Pattern 3: Monitoring and Alerting Integration**
Integrate with monitoring systems and alerting platforms.

```java
public class MonitoringIntegration implements TTLEventListener {
    
    private final AlertingService alertingService;
    private final MetricsCollector metricsCollector;
    
    public MonitoringIntegration(AlertingService alertingService, MetricsCollector metricsCollector) {
        this.alertingService = alertingService;
        this.metricsCollector = metricsCollector;
        
        // Register as event listener
        TTLIntegrationManager.getInstance().addEventListener(this);
    }
    
    @Override
    public void onTTLEvent(TTLEvent event) {
        // Collect metrics
        metricsCollector.recordTTLEvent(event.getEventType(), event.getTimestamp());
        
        // Send alerts for critical events
        if (isCriticalEvent(event)) {
            alertingService.sendAlert(createAlert(event));
        }
        
        // Update monitoring dashboards
        updateMonitoringDashboards(event);
    }
    
    private boolean isCriticalEvent(TTLEvent event) {
        return event.getEventType() == TTLEventType.GLOBAL_TTL_DISABLED ||
               event.getEventType() == TTLEventType.ALL_OVERRIDES_CLEARED;
    }
    
    private Alert createAlert(TTLEvent event) {
        return Alert.builder()
            .severity(AlertSeverity.WARNING)
            .message("TTL Management Event: " + event.getMessage())
            .reason(event.getReason())
            .timestamp(event.getTimestamp())
            .build();
    }
    
    private void updateMonitoringDashboards(TTLEvent event) {
        // Custom logic to update monitoring dashboards
        System.out.println("Updating dashboard with event: " + event.getEventType());
    }
}
```

### **Pattern 4: Scheduled Override Management**
Automatically manage TTL overrides with time-based expiration.

```java
public class ScheduledTTLManager {
    
    private final TTLIntegrationManager integrationManager;
    private final Map<String, ScheduledOverride> scheduledOverrides;
    
    public ScheduledTTLManager() {
        this.integrationManager = TTLIntegrationManager.getInstance();
        this.scheduledOverrides = new ConcurrentHashMap<>();
    }
    
    public void scheduleDebugMode(String overrideId, Class<?> clazz, 
                                 long durationMs, String reason) {
        TTLOverride debugOverride = TTLOverride.bypass();
        
        // Schedule the override
        integrationManager.scheduleOverride(overrideId, clazz, debugOverride, durationMs, reason);
        
        // Track locally for additional management
        scheduledOverrides.put(overrideId, new ScheduledOverride(overrideId, clazz, durationMs));
        
        System.out.println("Debug mode scheduled for " + clazz.getSimpleName() + 
                          " (duration: " + durationMs + "ms)");
    }
    
    public void scheduleBusinessHoursOverride(String overrideId, Class<?> clazz, 
                                            TTLOverride override, String reason) {
        // Calculate duration until end of business hours
        long durationMs = calculateBusinessHoursRemaining();
        
        // Schedule the override
        integrationManager.scheduleOverride(overrideId, clazz, override, durationMs, reason);
        
        System.out.println("Business hours override scheduled for " + clazz.getSimpleName());
    }
    
    private long calculateBusinessHoursRemaining() {
        // Custom logic to calculate remaining business hours
        // This is a simplified example
        return 8 * 60 * 60 * 1000; // 8 hours in milliseconds
    }
    
    private static class ScheduledOverride {
        private final String overrideId;
        private final Class<?> clazz;
        private final long durationMs;
        
        public ScheduledOverride(String overrideId, Class<?> clazz, long durationMs) {
            this.overrideId = overrideId;
            this.clazz = clazz;
            this.durationMs = durationMs;
        }
        
        // Getters...
    }
}
```

## 🎭 **Event-Driven Architecture**

### **Event Flow**
```
TTL Operation → TTLManager → TTLEventPublisher → Event Listeners → Custom Actions
```

### **Event Types and Use Cases**

| Event Type | Use Case | Example |
|------------|----------|---------|
| `GLOBAL_TTL_ENABLED` | Enable monitoring, send notifications | Production debugging started |
| `GLOBAL_TTL_DISABLED` | Disable monitoring, update dashboards | Production debugging completed |
| `CLASS_TTL_OVERRIDDEN` | Update class-specific monitoring | Enable debug for PaymentService |
| `METHOD_TTL_OVERRIDDEN` | Update method-specific monitoring | Enable debug for specific method |
| `TTL_OVERRIDE_SCHEDULED` | Setup temporary monitoring | Schedule cleanup tasks |
| `TTL_OVERRIDE_REMOVED` | Cleanup temporary monitoring | Remove scheduled tasks |

### **Event Data Payloads**
Each event contains:
- **Event Type**: Type of TTL operation
- **Message**: Human-readable description
- **Reason**: Business reason for the operation
- **Data**: Additional context-specific data
- **Timestamp**: When the event occurred

## 🛠️ **Custom Integration Examples**

### **Example 1: Customer Support Integration**
```java
public class CustomerSupportIntegration implements TTLEventListener {
    
    private final CustomerSupportService supportService;
    private final Map<String, SupportTicket> activeTickets;
    
    public CustomerSupportIntegration(CustomerSupportService supportService) {
        this.supportService = supportService;
        this.activeTickets = new ConcurrentHashMap<>();
        
        TTLIntegrationManager.getInstance().addEventListener(this);
    }
    
    @Override
    public void onTTLEvent(TTLEvent event) {
        if (event.getEventType() == TTLEventType.CLASS_TTL_OVERRIDDEN) {
            handleClassTTLOverride(event);
        }
    }
    
    private void handleClassTTLOverride(TTLEvent event) {
        TTLEventPublisher.ClassOverrideData data = 
            (TTLEventPublisher.ClassOverrideData) event.getData();
        
        if (data.getOverride().shouldBypass(null)) {
            // TTL bypassed - create support ticket
            SupportTicket ticket = supportService.createTicket(
                "TTL Debug Enabled", 
                "TTL bypassed for " + data.getClazz().getSimpleName(),
                event.getReason()
            );
            
            activeTickets.put(data.getClazz().getName(), ticket);
        }
    }
    
    public void closeDebugMode(Class<?> clazz) {
        SupportTicket ticket = activeTickets.remove(clazz.getName());
        if (ticket != null) {
            supportService.closeTicket(ticket.getId(), "Debug mode completed");
        }
    }
}
```

### **Example 2: Compliance and Audit Integration**
```java
public class ComplianceIntegration implements TTLEventListener {
    
    private final AuditService auditService;
    private final ComplianceService complianceService;
    
    public ComplianceIntegration(AuditService auditService, ComplianceService complianceService) {
        this.auditService = auditService;
        this.complianceService = complianceService;
        
        TTLIntegrationManager.getInstance().addEventListener(this);
    }
    
    @Override
    public void onTTLEvent(TTLEvent event) {
        // Log all TTL management events for compliance
        auditService.logEvent("TTL_MANAGEMENT", event.getEventType().name(), 
            event.getMessage(), event.getReason(), event.getTimestamp());
        
        // Check compliance rules
        checkComplianceRules(event);
    }
    
    private void checkComplianceRules(TTLEvent event) {
        if (event.getEventType() == TTLEventType.GLOBAL_TTL_DISABLED) {
            // Global TTL disabled - check if this violates compliance
            if (isComplianceViolation(event)) {
                complianceService.flagViolation("GLOBAL_TTL_DISABLED", 
                    event.getReason(), event.getTimestamp());
            }
        }
    }
    
    private boolean isComplianceViolation(TTLEvent event) {
        // Custom compliance logic
        return event.getReason().contains("production") && 
               !event.getReason().contains("emergency");
    }
}
```

### **Example 3: Performance Monitoring Integration**
```java
public class PerformanceMonitoringIntegration implements TTLEventListener {
    
    private final PerformanceMonitor performanceMonitor;
    private final AlertingService alertingService;
    
    public PerformanceMonitoringIntegration(PerformanceMonitor performanceMonitor, 
                                         AlertingService alertingService) {
        this.performanceMonitor = performanceMonitor;
        this.alertingService = alertingService;
        
        TTLIntegrationManager.getInstance().addEventListener(this);
    }
    
    @Override
    public void onTTLEvent(TTLEvent event) {
        // Monitor TTL management performance impact
        performanceMonitor.startOperation("TTL_MANAGEMENT_" + event.getEventType().name());
        
        // Check for performance degradation
        if (isPerformanceImpact(event)) {
            alertingService.sendAlert(Alert.builder()
                .severity(AlertSeverity.WARNING)
                .message("TTL management may impact performance")
                .reason("High frequency TTL operations detected")
                .build());
        }
    }
    
    private boolean isPerformanceImpact(TTLEvent event) {
        // Custom logic to detect performance impact
        // This could check frequency, timing, or other metrics
        return false; // Simplified for example
    }
}
```

## ⚙️ **Configuration Properties**

### **Integration Manager Properties**
```properties
# Enable/disable integration manager
ttl.integration.enabled=true

# Event publishing configuration
ttl.integration.events.enabled=true
ttl.integration.events.async=true
ttl.integration.events.max-listeners=100

# Scheduled override configuration
ttl.integration.scheduled.enabled=true
ttl.integration.scheduled.max-overrides=1000
ttl.integration.scheduled.cleanup-interval=300000

# Thread pool configuration
ttl.integration.thread-pool.core-size=2
ttl.integration.thread-pool.max-size=10
ttl.integration.thread-pool.queue-capacity=100
```

### **Event Publisher Properties**
```properties
# Event publisher configuration
ttl.events.publisher.enabled=true
ttl.events.publisher.async=true
ttl.events.publisher.max-listeners=100
ttl.events.publisher.error-handling=log

# Event filtering
ttl.events.filter.enabled=true
ttl.events.filter.include-types=GLOBAL_TTL_ENABLED,CLASS_TTL_OVERRIDDEN
ttl.events.filter.exclude-types=SYSTEM_STARTED
```

## 🎯 **Best Practices**

### **1. Event Listener Design**
- **Keep listeners lightweight**: Avoid heavy operations in event handlers
- **Handle exceptions gracefully**: Don't let listener errors break the system
- **Use async processing**: For heavy operations, use async processing
- **Implement idempotency**: Ensure listeners can handle duplicate events

### **2. Resource Management**
- **Proper cleanup**: Always remove listeners when components are destroyed
- **Shutdown gracefully**: Properly shutdown integration managers
- **Monitor memory usage**: Be aware of listener accumulation
- **Use weak references**: Consider weak references for long-lived listeners

### **3. Error Handling**
- **Log errors**: Always log errors in event listeners
- **Circuit breakers**: Implement circuit breakers for external integrations
- **Fallback mechanisms**: Provide fallbacks when integrations fail
- **Health checks**: Monitor integration health

### **4. Performance Considerations**
- **Batch operations**: Batch multiple TTL operations when possible
- **Async processing**: Use async processing for non-critical operations
- **Event filtering**: Filter events to only process relevant ones
- **Resource pooling**: Pool resources for external integrations

## 🔄 **Migration Guide**

### **From Spring Boot to Non-Spring**
If you're migrating from Spring Boot to a non-Spring application:

1. **Replace Spring Services**:
   ```java
   // Before (Spring Boot)
   @Autowired
   private TTLManagementService ttlService;
   
   // After (Non-Spring)
   private TTLIntegrationManager integrationManager = TTLIntegrationManager.getInstance();
   ```

2. **Replace REST Controllers**:
   ```java
   // Before (Spring Boot)
   @PostMapping("/api/ttl/enable")
   public ResponseEntity<?> enableTTL() {
       ttlService.enableAllTTL("REST API call");
       return ResponseEntity.ok().build();
   }
   
   // After (Non-Spring)
   public void enableTTL() {
       integrationManager.enableAllTTL("Direct method call");
   }
   ```

3. **Replace Event Listeners**:
   ```java
   // Before (Spring Boot)
   @EventListener
   public void handleTTLEvent(TTLManagementEvent event) {
       // Handle event
   }
   
   // After (Non-Spring)
   public void handleTTLEvent() {
       integrationManager.addEventListener(event -> {
           // Handle event
       });
   }
   ```

### **Integration Patterns Migration**
1. **Event-driven**: Use `TTLEventListener` interface
2. **Scheduled**: Use `scheduleOverride` methods
3. **Monitoring**: Use `getStatus()` and event listeners
4. **Configuration**: Use direct method calls instead of REST endpoints

## 🚀 **Quick Start**

### **1. Add Dependencies**
```xml
<dependency>
    <groupId>io.github.krishnachaitanyap</groupId>
    <artifactId>logger-ttl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **2. Initialize Integration Manager**
```java
public class Application {
    public static void main(String[] args) {
        // Integration manager is automatically initialized
        TTLIntegrationManager integrationManager = TTLIntegrationManager.getInstance();
        
        // Add event listeners
        integrationManager.addEventListener(new CustomTTLListener());
        
        // Start your application
        startApplication();
    }
}
```

### **3. Create Custom Integrations**
```java
public class CustomTTLListener implements TTLEventListener {
    @Override
    public void onTTLEvent(TTLEvent event) {
        // Your custom integration logic
        System.out.println("TTL Event: " + event.getEventType());
    }
}
```

### **4. Manage TTL During Runtime**
```java
public class TTLManager {
    private final TTLIntegrationManager integrationManager;
    
    public TTLManager() {
        this.integrationManager = TTLIntegrationManager.getInstance();
    }
    
    public void enableDebugMode(String reason) {
        integrationManager.enableAllTTL(reason);
        integrationManager.extendGlobalTTL(1, "Debug mode: " + reason);
    }
    
    public void enableClassDebug(Class<?> clazz, String reason) {
        TTLOverride override = TTLOverride.bypass();
        integrationManager.overrideClassTTL(clazz, override, reason);
    }
}
```

## 📚 **Additional Resources**

- [Runtime TTL Management Guide](RUNTIME_TTL_MANAGEMENT.md)
- [Spring Boot Integration Guide](SPRING_BOOT_INTEGRATION.md)
- [API Reference Documentation](API_REFERENCE.md)
- [Examples and Use Cases](EXAMPLES.md)

---

This integration layer provides a powerful, flexible way to build custom TTL management solutions without Spring Boot dependencies, while maintaining the same functionality and capabilities.

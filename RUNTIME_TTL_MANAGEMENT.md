# 🚀 Runtime TTL Management

## Overview

The Logger TTL Framework now supports **dynamic runtime management** of TTL rules, allowing you to re-enable expired logs, extend TTL periods, and override configurations without restarting your application.

## 🎯 **Use Cases**

### 1. **Production Debugging**
```java
// Enable all expired logs temporarily for investigation
TTLManager.getInstance().enableAllTTL();

// Debug logs now work even if they were expired
logger.debug("This DEBUG log was expired but now works");
logger.info("This INFO log was expired but now works");

// Restore normal behavior after investigation
TTLManager.getInstance().disableAllTTL();
```

### 2. **Compliance Audits**
```java
// Extend all TTL by 90 days for audit purposes
TTLManager.getInstance().setGlobalTTLExtension(90);

// All logs now have extended TTL
logger.info("This log TTL extended by 90 days for audit");

// Remove extension after audit
TTLManager.getInstance().setGlobalTTLExtension(0);
```

### 3. **Temporary Overrides**
```java
// Override specific class TTL to never expire
TTLOverride override = TTLOverride.bypass();
TTLManager.getInstance().overrideClassTTL(MyService.class, override);

// Override specific method TTL to extend by 30 days
TTLOverride methodOverride = TTLOverride.extend(30);
TTLManager.getInstance().overrideMethodTTL(MyService.class, "processData", methodOverride);
```

## 🏗️ **Architecture**

### Core Components

1. **`TTLManager`** - Singleton manager for runtime TTL control
2. **`TTLOverride`** - Defines override rules and types
3. **Enhanced `TTLAnnotationProcessor`** - Integrates runtime overrides with annotation processing

### Override Types

| Type | Description | Example |
|------|-------------|---------|
| **`BYPASS`** | Completely bypass TTL rules | `TTLOverride.bypass()` |
| **`EXTEND`** | Add extra days to existing TTL | `TTLOverride.extend(30)` |
| **`REPLACE`** | Use completely new TTL configuration | `TTLOverride.replace("2025-12-31T00:00:00Z", 365, LogLevel.ERROR)` |

## 📚 **API Reference**

### TTLManager

#### Global Controls
```java
// Enable/disable all TTL rules globally
TTLManager manager = TTLManager.getInstance();
manager.enableAllTTL();        // Bypass all TTL rules
manager.disableAllTTL();       // Enforce all TTL rules
manager.isGlobalTTLEnabled();  // Check global override status

// Extend all TTL globally
manager.setGlobalTTLExtension(15);  // Add 15 days to all TTL
manager.getGlobalTTLExtension();    // Get current extension
```

#### Class-Level Overrides
```java
// Override TTL for specific class
TTLOverride classOverride = TTLOverride.bypass();
manager.overrideClassTTL(MyService.class, classOverride);

// Remove class override
manager.removeClassTTLOverride(MyService.class);

// Get class override
TTLOverride override = manager.getClassTTLOverride(MyService.class);
```

#### Method-Level Overrides
```java
// Override TTL for specific method
TTLOverride methodOverride = TTLOverride.extend(20);
manager.overrideMethodTTL(MyService.class, "processData", methodOverride);

// Remove method override
manager.removeMethodTTLOverride(MyService.class, "processData");

// Get method override
TTLOverride override = manager.getMethodTTLOverride(MyService.class, "processData");
```

#### Field-Level Overrides
```java
// Override TTL for specific field
TTLOverride fieldOverride = TTLOverride.replace("2025-06-01T00:00:00Z", 60, LogLevel.WARN);
manager.overrideFieldTTL(MyService.class, "logger", fieldOverride);

// Remove field override
manager.removeFieldTTLOverride(MyService.class, "logger");

// Get field override
TTLOverride override = manager.getFieldTTLOverride(MyService.class, "logger");
```

#### Utility Methods
```java
// Clear all overrides
manager.clearAllOverrides();

// Get summary of current overrides
String summary = manager.getOverridesSummary();

// Check if TTL should be bypassed for specific context
boolean shouldBypass = manager.shouldBypassTTL(MyService.class, "method", "field", config);

// Apply global extension to TTL config
TTLConfig extended = manager.applyGlobalExtension(originalConfig);
```

### TTLOverride

#### Factory Methods
```java
// Create bypass override (always log)
TTLOverride bypass = TTLOverride.bypass();

// Create extension override (add days)
TTLOverride extend = TTLOverride.extend(30);

// Create replacement override (new config)
TTLOverride replace = TTLOverride.replace("2025-12-31T00:00:00Z", 365, LogLevel.ERROR);
```

#### Instance Methods
```java
// Check if override should bypass TTL
boolean shouldBypass = override.shouldBypass(originalConfig);

// Apply override to original config
TTLConfig result = override.apply(originalConfig);

// Get override details
OverrideType type = override.getType();
int extraDays = override.getExtraDays();
String newStartDate = override.getNewStartDate();
int newTtlDays = override.getNewTtlDays();
LogLevel[] newLevels = override.getNewLevels();
Instant overrideTime = override.getOverrideTime();
```

## 🔄 **Integration with Existing Framework**

### Automatic Integration
The runtime TTL management is **automatically integrated** with the existing framework:

1. **`TTLAnnotationProcessor`** now checks runtime overrides before returning TTL configurations
2. **All existing annotations** continue to work as before
3. **Runtime overrides** take precedence over annotation-based configurations
4. **Seamless operation** - no changes needed to existing code

### Processing Order
1. **Runtime Overrides** (highest priority)
   - Global TTL override
   - Class-level override
   - Method-level override
   - Field-level override
2. **Annotation-Based Config** (original priority)
   - Field-level annotation
   - Method-level annotation
   - Class-level annotation
3. **Default Config** (fallback)

## 🧪 **Examples**

### Complete Runtime Management Demo
```java
@LogTTL(start = "2025-01-01T00:00:00Z", ttlDays = 30)
public class RuntimeTTLDemo {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(RuntimeTTLDemo.class);
    
    public void demonstrateRuntimeControl() {
        TTLManager manager = TTLManager.getInstance();
        
        // Scenario 1: Enable all TTL rules for debugging
        logger.info("Production issue detected - enabling all logs");
        manager.enableAllTTL();
        
        // Now all logs work, including expired ones
        logger.debug("This DEBUG log was expired but now works");
        logger.info("This INFO log was expired but now works");
        
        // Scenario 2: Extend TTL for compliance audit
        logger.info("Starting compliance audit - extending TTL by 90 days");
        manager.disableAllTTL(); // Disable global override
        manager.setGlobalTTLExtension(90);
        
        // All logs now have extended TTL
        logger.info("This log TTL extended by 90 days for audit");
        
        // Scenario 3: Override specific method TTL
        logger.info("Overriding method TTL to extend by 30 days");
        TTLOverride methodOverride = TTLOverride.extend(30);
        manager.overrideMethodTTL(RuntimeTTLDemo.class, "demonstrateRuntimeControl", methodOverride);
        
        // This method now has extended TTL
        logger.debug("Method TTL extended by 30 days");
        
        // Clean up after operations
        logger.info("Operations complete - cleaning up overrides");
        manager.clearAllOverrides();
    }
}
```

### Production Debugging Workflow
```java
public class ProductionDebugger {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(ProductionDebugger.class);
    
    public void enableDebugMode() {
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("🔧 Enabling production debug mode");
        manager.enableAllTTL();
        
        // Log current status
        logger.info("TTL Status: " + manager.getOverridesSummary());
        
        // Now all expired logs work
        logger.debug("DEBUG logs enabled for investigation");
        logger.info("INFO logs enabled for investigation");
    }
    
    public void disableDebugMode() {
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("🔧 Disabling production debug mode");
        manager.disableAllTTL();
        
        // Log final status
        logger.info("TTL Status: " + manager.getOverridesSummary());
        logger.info("Debug mode disabled - normal TTL rules restored");
    }
    
    public void extendTTLForAudit(int extraDays) {
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("📋 Extending TTL by " + extraDays + " days for audit");
        manager.setGlobalTTLExtension(extraDays);
        
        logger.info("TTL extended - all logs now have +" + extraDays + " days");
    }
}
```

## 🚨 **Best Practices**

### 1. **Use Global Overrides Sparingly**
```java
// ❌ Avoid: Long-term global overrides
manager.enableAllTTL(); // This disables all TTL rules

// ✅ Prefer: Targeted overrides
manager.overrideClassTTL(SpecificService.class, TTLOverride.bypass());
```

### 2. **Clean Up Overrides**
```java
// Always clean up overrides after use
try {
    manager.enableAllTTL();
    // Perform debugging operations
} finally {
    manager.disableAllTTL();
}
```

### 3. **Monitor Override Status**
```java
// Log override status for debugging
logger.info("TTL Override Status: " + manager.getOverridesSummary());

// Check before applying overrides
if (manager.isGlobalTTLEnabled()) {
    logger.warn("Global TTL override is already enabled");
}
```

### 4. **Use Appropriate Override Types**
```java
// For temporary debugging
TTLOverride.bypass()

// For extending existing TTL
TTLOverride.extend(extraDays)

// For completely new configuration
TTLOverride.replace(startDate, ttlDays, levels)
```

## 🔒 **Security Considerations**

### 1. **Access Control**
- Runtime TTL management should be restricted to authorized users
- Consider implementing authentication for TTL management operations
- Log all TTL override operations for audit purposes

### 2. **Resource Management**
- Runtime overrides consume memory (stored in ConcurrentMaps)
- Clear overrides when no longer needed
- Monitor memory usage in long-running applications

### 3. **Audit Trail**
```java
// Log all TTL management operations
logger.info("TTL Override applied: " + override.toString());
logger.info("TTL Override removed for: " + clazz.getName());
logger.info("Global TTL extension set to: " + extraDays + " days");
```

## 📊 **Performance Impact**

### Minimal Overhead
- **Runtime overrides** add minimal overhead to TTL processing
- **Concurrent data structures** ensure thread safety without blocking
- **Lazy evaluation** - overrides only checked when needed

### Memory Usage
- **Per-override memory**: ~100-200 bytes per override
- **Global state**: ~50 bytes for global settings
- **Total overhead**: Negligible for typical usage patterns

## 🔧 **Troubleshooting**

### Common Issues

1. **Overrides Not Working**
   ```java
   // Check if overrides are properly set
   String summary = manager.getOverridesSummary();
   logger.info("TTL Status: " + summary);
   
   // Verify override was applied
   TTLOverride override = manager.getClassTTLOverride(MyClass.class);
   if (override != null) {
       logger.info("Override found: " + override.toString());
   }
   ```

2. **Global Override Not Working**
   ```java
   // Check global override status
   if (manager.isGlobalTTLEnabled()) {
       logger.info("Global TTL override is enabled");
   } else {
       logger.info("Global TTL override is disabled");
   }
   ```

3. **Memory Leaks**
   ```java
   // Clear overrides periodically
   manager.clearAllOverrides();
   
   // Or remove specific overrides
   manager.removeClassTTLOverride(MyClass.class);
   ```

## 🚀 **Future Enhancements**

### Planned Features
1. **TTL Override Persistence** - Save/restore override configurations
2. **TTL Override Scheduling** - Automatic override expiration
3. **TTL Override Metrics** - Monitor override usage and impact
4. **TTL Override Templates** - Predefined override configurations
5. **TTL Override Validation** - Prevent invalid override combinations

### Extension Points
The framework is designed for easy extension:
- **Custom Override Types** - Implement new override behaviors
- **Override Persistence** - Store overrides in databases or files
- **Override Management UI** - Web-based override management
- **Override APIs** - REST endpoints for TTL management

## 📚 **Related Documentation**

- [README.md](README.md) - Framework overview and basic usage
- [PUBLISHING_GUIDE.md](PUBLISHING_GUIDE.md) - Publishing to Maven repositories
- [GITHUB_PUBLISHING.md](GITHUB_PUBLISHING.md) - GitHub Packages specific guide

## 🎉 **Conclusion**

Runtime TTL management transforms the Logger TTL Framework from a static annotation-based system to a **dynamic, production-ready logging solution**. You can now:

- **Debug production issues** by enabling expired logs
- **Extend TTL periods** for compliance and audit requirements
- **Override TTL configurations** without code changes
- **Manage logging behavior** dynamically based on operational needs

This feature makes the framework suitable for **enterprise production environments** where logging requirements can change based on operational needs, compliance requirements, or debugging scenarios.

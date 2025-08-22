# Logger TTL Framework

A Time-To-Live (TTL) logging framework built on top of SLF4J/Log4j that allows developers to automatically expire log statements based on configurable time windows and log levels.

## Features

- **Automatic Log Expiration**: Logs automatically expire after a specified time period
- **Flexible Annotation System**: Apply TTL rules at class, method, or field levels
- **Level-Specific TTL**: Apply TTL rules to specific log levels only
- **Start Date Support**: Configure when TTL rules become active
- **Drop-in Replacement**: Seamlessly replace existing SLF4J LoggerFactory usage
- **Runtime Reflection**: Automatic TTL configuration detection using reflection
- **Explicit TTL Configuration**: Programmatic TTL configuration for dynamic scenarios

## Requirements

- Java 11 or higher
- SLF4J 2.0.9+
- Log4j 2.20.0+

## Quick Start

### 1. Add Dependencies

```xml
<dependency>
    <groupId>com.logger</groupId>
    <artifactId>logger-ttl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Replace LoggerFactory

```java
// Before
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

// After
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

private static final TTLLogger logger = TTLLoggerFactory.getLogger(MyClass.class);
```

### 3. Add TTL Annotations

```java
@LogTTL(start="2025-08-20T00:00:00Z", ttlDays=30, levels={LogLevel.DEBUG, LogLevel.INFO})
public class PaymentService {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(PaymentService.class);
    
    public void processPayment() {
        logger.info("This log expires in 30 days");   // ✅ expires
        logger.debug("This log expires in 30 days");  // ✅ expires
        logger.error("This log is permanent");        // ❌ unaffected
    }
}
```

## Annotation Usage

### @LogTTL Parameters

- **`start`** (optional): ISO8601 start date string (e.g., "2025-08-20T00:00:00Z")
  - If empty or not specified, no start restriction is applied
- **`ttlDays`** (optional): Number of days until expiration (default: -1 = never expires)
- **`levels`** (optional): Array of log levels to which TTL applies
  - If empty, TTL applies to all levels

### Annotation Scopes

#### 1. Class-Level TTL

```java
@LogTTL(ttlDays=30, levels={LogLevel.DEBUG, LogLevel.INFO})
public class UserService {
    // All DEBUG and INFO logs in this class expire in 30 days
    // Other levels are unaffected
}
```

#### 2. Method-Level TTL

```java
public class OrderService {
    
    @LogTTL(ttlDays=7, levels={LogLevel.DEBUG})
    public void processOrder() {
        // Only DEBUG logs in this method expire in 7 days
        // Other levels and methods are unaffected
    }
    
    @LogTTL(start="2025-01-01T00:00:00Z", ttlDays=60)
    public void validateOrder() {
        // All logs in this method are valid from 2025-01-01 and expire in 60 days
    }
}
```

#### 3. Field-Level TTL

```java
public class NotificationService {
    
    @LogTTL(ttlDays=15, levels={LogLevel.INFO, LogLevel.WARN})
    private static final TTLLogger infoLogger = TTLLoggerFactory.getLogger(NotificationService.class);
    
    @LogTTL(ttlDays=30)
    private static final TTLLogger generalLogger = TTLLoggerFactory.getLogger(NotificationService.class);
    
    public void sendNotification() {
        // infoLogger: INFO and WARN logs expire in 15 days
        // generalLogger: all logs expire in 30 days
    }
}
```

## Explicit TTL Configuration

For dynamic scenarios where annotations aren't suitable, you can configure TTL programmatically:

```java
// Create logger with explicit TTL configuration
TTLLogger logger = TTLLoggerFactory.getLogger(
    MyClass.class,
    "2025-01-01T00:00:00Z",  // start date
    30,                       // ttl days
    LogLevel.INFO,            // affected levels
    LogLevel.WARN
);

// Use explicit TTL for individual log statements
logger.info("Message", "2025-01-01T00:00:00Z", 30, LogLevel.INFO);
```

## TTL Behavior Rules

1. **Priority Order**: Field-level > Method-level > Class-level
2. **Level Filtering**: If `levels` is specified, TTL only applies to those levels
3. **Start Date**: If `start` is specified, TTL rules are only active after that date
4. **Expiration**: Logs expire after `start + ttlDays`
5. **Default Behavior**: If no TTL is configured, logs are always executed

## Examples

### Payment Service with Class-Level TTL

```java
@LogTTL(start="2025-08-20T00:00:00Z", ttlDays=30, levels={LogLevel.DEBUG, LogLevel.INFO})
public class PaymentService {

    private static final TTLLogger logger = TTLLoggerFactory.getLogger(PaymentService.class);

    public void processPayment() {
        logger.info("This INFO log expires in 30 days");   // ✅ expires
        logger.debug("This DEBUG log expires in 30 days"); // ✅ expires
        logger.error("This ERROR log is permanent");       // ❌ unaffected
    }
}
```

### User Service with Method-Level TTL

```java
public class UserService {

    private static final TTLLogger logger = TTLLoggerFactory.getLogger(UserService.class);

    @LogTTL(ttlDays=7, levels={LogLevel.DEBUG})
    public void getUser() {
        logger.debug("DEBUG log auto-expires in 7 days"); // ✅ expires
        logger.info("INFO log is permanent");             // ❌ unaffected
    }
}
```

### Order Service with Field-Level TTL

```java
public class OrderService {

    @LogTTL(ttlDays=15, levels={LogLevel.DEBUG, LogLevel.INFO})
    private static final TTLLogger debugLogger = TTLLoggerFactory.getLogger(OrderService.class);
    
    @LogTTL(ttlDays=30)
    private static final TTLLogger generalLogger = TTLLoggerFactory.getLogger(OrderService.class);

    public void processOrder() {
        // debugLogger: DEBUG and INFO expire in 15 days
        // generalLogger: all levels expire in 30 days
    }
}
```

## Architecture

### Core Components

1. **`@LogTTL`**: Annotation for configuring TTL behavior
2. **`TTLConfig`**: Encapsulates TTL configuration and validation logic
3. **`TTLAnnotationProcessor`**: Uses reflection to inspect annotations at runtime
4. **`TTLLogger`**: Wraps SLF4J Logger with TTL functionality
5. **`TTLLoggerFactory`**: Factory for creating TTL-enabled loggers

### How It Works

1. **Annotation Processing**: At runtime, the framework inspects the call stack to find `@LogTTL` annotations
2. **Configuration Resolution**: TTL configuration is resolved based on annotation hierarchy and scope
3. **TTL Validation**: Before logging, the framework checks if the current time is within the valid TTL window
4. **Level Filtering**: If specific levels are configured, TTL rules only apply to those levels
5. **Log Execution**: Log statements are executed only if they pass TTL validation

### Reflection Strategy

The framework uses reflection to:
- Inspect class-level annotations
- Find method-level annotations
- Locate field-level annotations on logger instances
- Determine the calling context for proper TTL resolution

## Testing

Run the test suite to verify TTL functionality:

```bash
mvn test
```

The test suite covers:
- TTL configuration validation
- Annotation processing
- Logger behavior with different TTL settings
- Edge cases and error conditions

## Performance Considerations

- **Reflection Overhead**: Minimal impact as reflection is only used during TTL configuration resolution
- **TTL Validation**: Fast timestamp comparison with negligible performance cost
- **Memory Usage**: TTL configurations are lightweight and cached per logger instance

## Best Practices

1. **Use Class-Level TTL** for services with consistent logging requirements
2. **Use Method-Level TTL** for operations with different retention needs
3. **Use Field-Level TTL** for fine-grained control over specific logger instances
4. **Set Appropriate TTL Values** based on business requirements and compliance needs
5. **Monitor TTL Behavior** in production to ensure logs expire as expected

## Migration Guide

### From SLF4J LoggerFactory

```java
// Old code
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void doSomething() {
        logger.info("Processing request");
        logger.debug("Request details: {}", request);
    }
}

// New code
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

public class MyService {
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(MyService.class);
    
    public void doSomething() {
        logger.info("Processing request");
        logger.debug("Request details: {}", request);
    }
}
```

### Adding TTL Annotations

```java
// Add TTL to specific methods
@LogTTL(ttlDays=7, levels={LogLevel.DEBUG})
public void debugMethod() {
    logger.debug("This will expire in 7 days");
}

// Add TTL to entire class
@LogTTL(ttlDays=30, levels={LogLevel.INFO, LogLevel.WARN})
public class MyService {
    // All INFO and WARN logs expire in 30 days
}
```

## Troubleshooting

### Common Issues

1. **Logs Not Expiring**: Check TTL configuration and start dates
2. **Reflection Errors**: Ensure proper class loading and annotation retention
3. **Performance Issues**: Monitor TTL validation overhead in high-throughput scenarios

### Debug Mode

Enable debug logging to see TTL processing:

```java
// Set log level to DEBUG for TTL framework
logging.level.com.logger.ttl=DEBUG
```

## Project Structure

```
src/
├── main/java/com/logger/ttl/
│   ├── LogLevel.java              # Log level enumeration
│   ├── LogTTL.java                # TTL annotation definition
│   ├── TTLConfig.java             # TTL configuration class
│   ├── TTLAnnotationProcessor.java # Annotation processing logic
│   ├── TTLLogger.java             # Main logger wrapper
│   ├── TTLLoggerFactory.java      # Logger factory
│   ├── TTLManager.java            # Runtime TTL management
│   ├── TTLOverride.java           # TTL override definitions
│   ├── integration/               # Non-Spring integration layer
│   └── examples/                  # Examples and demos
│       ├── demo/                  # Demo applications
│       ├── UserService.java       # Example service
│       ├── OrderService.java      # Example service
│       └── PaymentService.java    # Example service
├── test/java/com/logger/ttl/      # Unit tests
└── docs/                          # Documentation and scripts
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions and support:
- Create an issue on GitHub
- Check the documentation
- Review the test examples

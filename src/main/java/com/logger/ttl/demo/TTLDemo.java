package com.logger.ttl.demo;

import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

/**
 * Demo application showcasing the TTL logging framework.
 * 
 * <p>This demo demonstrates various TTL configurations and their effects
 * on different log levels and scopes.</p>
 */
public class TTLDemo {
    
    public static void main(String[] args) {
        System.out.println("=== TTL Logging Framework Demo ===\n");
        
        // Demo class-level TTL
        demoClassLevelTTL();
        
        // Demo method-level TTL
        demoMethodLevelTTL();
        
        // Demo field-level TTL
        FieldLevelTTLDemo.runDemo();
        
        // Demo explicit TTL configuration
        demoExplicitTTL();
        
        System.out.println("\n=== Demo Complete ===");
        System.out.println("Check the logs to see TTL behavior in action!");
    }
    
    /**
     * Demonstrates class-level TTL annotation.
     */
    @LogTTL(ttlDays=30, levels={LogLevel.DEBUG, LogLevel.INFO})
    private static void demoClassLevelTTL() {
        System.out.println("--- Class-Level TTL Demo ---");
        
        TTLLogger logger = TTLLoggerFactory.getLogger(TTLDemo.class);
        
        logger.debug("Class-level DEBUG log - expires in 30 days");
        logger.info("Class-level INFO log - expires in 30 days");
        logger.warn("Class-level WARN log - permanent (not affected by TTL)");
        logger.error("Class-level ERROR log - permanent (not affected by TTL)");
        
        System.out.println("Class-level TTL demo completed.\n");
    }
    
    /**
     * Demonstrates method-level TTL annotation.
     */
    private static void demoMethodLevelTTL() {
        System.out.println("--- Method-Level TTL Demo ---");
        
        TTLLogger logger = TTLLoggerFactory.getLogger(TTLDemo.class);
        
        // Method with TTL
        demoMethodWithTTL(logger);
        
        // Method without TTL
        demoMethodWithoutTTL(logger);
        
        System.out.println("Method-level TTL demo completed.\n");
    }
    
    @LogTTL(ttlDays=7, levels={LogLevel.DEBUG})
    private static void demoMethodWithTTL(TTLLogger logger) {
        System.out.println("  Method with TTL (7 days, DEBUG only):");
        logger.debug("Method TTL DEBUG log - expires in 7 days");
        logger.info("Method TTL INFO log - permanent (not affected by TTL)");
        logger.warn("Method TTL WARN log - permanent (not affected by TTL)");
    }
    
    private static void demoMethodWithoutTTL(TTLLogger logger) {
        System.out.println("  Method without TTL:");
        logger.debug("No TTL DEBUG log - permanent");
        logger.info("No TTL INFO log - permanent");
        logger.warn("No TTL WARN log - permanent");
    }
    

    
    /**
     * Demonstrates explicit TTL configuration.
     */
    private static void demoExplicitTTL() {
        System.out.println("--- Explicit TTL Configuration Demo ---");
        
        // Create logger with explicit TTL configuration
        TTLLogger explicitLogger = TTLLoggerFactory.getLogger(
            TTLDemo.class,
            "", // no start date
            21, // 21 days TTL
            LogLevel.INFO, LogLevel.WARN
        );
        
        System.out.println("  Explicit TTL Logger (21 days, INFO/WARN only):");
        explicitLogger.debug("Explicit TTL DEBUG log - permanent (not affected)");
        explicitLogger.info("Explicit TTL INFO log - expires in 21 days");
        explicitLogger.warn("Explicit TTL WARN log - expires in 21 days");
        explicitLogger.error("Explicit TTL ERROR log - permanent (not affected)");
        
        // Test individual log statements with explicit TTL
        System.out.println("  Individual log statements with explicit TTL:");
        explicitLogger.info("Individual TTL log", "2025-01-01T00:00:00Z", 30, LogLevel.INFO);
        explicitLogger.debug("Individual TTL log", "", 14, LogLevel.DEBUG);
        
        System.out.println("Explicit TTL demo completed.\n");
    }
}

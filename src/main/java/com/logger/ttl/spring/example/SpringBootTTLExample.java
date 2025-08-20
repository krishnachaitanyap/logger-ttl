package com.logger.ttl.spring.example;

import com.logger.ttl.*;
import com.logger.ttl.spring.TTLManagementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Complete Spring Boot example application demonstrating Runtime TTL Management.
 * 
 * This example shows how to:
 * 1. Enable expired logs for production debugging
 * 2. Override TTL for specific classes and methods
 * 3. Schedule automatic TTL overrides
 * 4. Use REST APIs for TTL management
 * 5. Monitor TTL management status
 */
@SpringBootApplication
@EnableScheduling
public class SpringBootTTLExample {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootTTLExample.class, args);
    }
    
    @Bean
    public CommandLineRunner demoRunner(TTLManagementService ttlService) {
        return args -> {
            System.out.println("🚀 Spring Boot TTL Management Example Started!");
            System.out.println("================================================");
            
            // Run the demo
            runTTLManagementDemo(ttlService);
        };
    }
    
    private void runTTLManagementDemo(TTLManagementService ttlService) {
        try {
            // Wait for Spring Boot to fully start
            Thread.sleep(2000);
            
            System.out.println("\n📋 Starting TTL Management Demo...");
            
            // Demo 1: Production Debugging
            demonstrateProductionDebugging(ttlService);
            
            // Demo 2: Class-Level TTL Override
            demonstrateClassLevelOverride(ttlService);
            
            // Demo 3: Method-Level TTL Override
            demonstrateMethodLevelOverride(ttlService);
            
            // Demo 4: Scheduled TTL Override
            demonstrateScheduledOverride(ttlService);
            
            // Demo 5: Global TTL Extension
            demonstrateGlobalTTLExtension(ttlService);
            
            // Demo 6: Status Monitoring
            demonstrateStatusMonitoring(ttlService);
            
            // Cleanup
            System.out.println("\n🧹 Cleaning up all TTL overrides...");
            ttlService.clearAllOverrides("Demo cleanup");
            
            System.out.println("\n✅ TTL Management Demo Complete!");
            System.out.println("Check the logs above to see TTL management in action.");
            System.out.println("\n🌐 REST API Endpoints Available:");
            System.out.println("  GET  /api/ttl/status");
            System.out.println("  POST /api/ttl/enable");
            System.out.println("  POST /api/ttl/disable");
            System.out.println("  POST /api/ttl/extend");
            System.out.println("  POST /api/ttl/class/{className}/override");
            System.out.println("  POST /api/ttl/method/{className}/{methodName}/override");
            System.out.println("  POST /api/ttl/schedule");
            System.out.println("  DELETE /api/ttl/clear");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void demonstrateProductionDebugging(TTLManagementService ttlService) {
        System.out.println("\n🔧 Demo 1: Production Debugging");
        System.out.println("--------------------------------");
        
        // Show initial status
        System.out.println("Initial TTL Status:");
        printTTLStatus(ttlService.getStatus());
        
        // Enable all TTL rules for debugging
        System.out.println("\nEnabling all TTL rules for production debugging...");
        ttlService.enableAllTTL("Production issue investigation");
        
        // Show updated status
        System.out.println("Updated TTL Status:");
        printTTLStatus(ttlService.getStatus());
        
        // Disable debugging mode
        System.out.println("\nDisabling production debug mode...");
        ttlService.disableAllTTL("Investigation complete");
        
        // Show final status
        System.out.println("Final TTL Status:");
        printTTLStatus(ttlService.getStatus());
    }
    
    private void demonstrateClassLevelOverride(TTLManagementService ttlService) {
        System.out.println("\n📝 Demo 2: Class-Level TTL Override");
        System.out.println("------------------------------------");
        
        // Override TTL for String class to never expire
        System.out.println("Overriding String class TTL to never expire...");
        TTLOverride bypassOverride = TTLOverride.bypass();
        ttlService.overrideClassTTL(String.class, bypassOverride, "String class debugging");
        
        // Show class override
        TTLOverride retrieved = ttlService.getClassTTLOverride(String.class);
        System.out.println("String class TTL override: " + retrieved);
        
        // Extend TTL for Integer class
        System.out.println("Extending Integer class TTL by 15 days...");
        TTLOverride extendOverride = TTLOverride.extend(15);
        ttlService.overrideClassTTL(Integer.class, extendOverride, "Integer class audit");
        
        // Show class override
        retrieved = ttlService.getClassTTLOverride(Integer.class);
        System.out.println("Integer class TTL override: " + retrieved);
    }
    
    private void demonstrateMethodLevelOverride(TTLManagementService ttlService) {
        System.out.println("\n🔍 Demo 3: Method-Level TTL Override");
        System.out.println("--------------------------------------");
        
        // Override TTL for specific method
        System.out.println("Overriding toString method TTL to extend by 30 days...");
        TTLOverride methodOverride = TTLOverride.extend(30);
        ttlService.overrideMethodTTL(String.class, "toString", methodOverride, "toString method debugging");
        
        // Show method override
        TTLOverride retrieved = ttlService.getMethodTTLOverride(String.class, "toString");
        System.out.println("toString method TTL override: " + retrieved);
        
        // Override another method
        System.out.println("Overriding hashCode method TTL to bypass...");
        TTLOverride bypassOverride = TTLOverride.bypass();
        ttlService.overrideMethodTTL(String.class, "hashCode", bypassOverride, "hashCode method investigation");
        
        // Show method override
        retrieved = ttlService.getMethodTTLOverride(String.class, "hashCode");
        System.out.println("hashCode method TTL override: " + retrieved);
    }
    
    private void demonstrateScheduledOverride(TTLManagementService ttlService) {
        System.out.println("\n⏰ Demo 4: Scheduled TTL Override");
        System.out.println("----------------------------------");
        
        // Schedule a TTL override that will be automatically removed
        System.out.println("Scheduling TTL override for Long class for 10 seconds...");
        TTLOverride scheduledOverride = TTLOverride.bypass();
        ttlService.scheduleTTLOverride(
            "demo-scheduled-override",
            Long.class,
            scheduledOverride,
            10000, // 10 seconds
            "Temporary debugging session"
        );
        
        // Show scheduled override
        System.out.println("Scheduled override applied. It will be automatically removed in 10 seconds.");
        System.out.println("Active scheduled overrides: " + ttlService.getScheduledOverridesCount());
        
        // Wait a bit to show the override is active
        try {
            Thread.sleep(2000);
            System.out.println("Override is still active. Active overrides: " + ttlService.getScheduledOverridesCount());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void demonstrateGlobalTTLExtension(TTLManagementService ttlService) {
        System.out.println("\n📅 Demo 5: Global TTL Extension");
        System.out.println("--------------------------------");
        
        // Extend all TTL globally
        System.out.println("Extending all TTL globally by 45 days...");
        ttlService.extendGlobalTTL(45, "Compliance audit period");
        
        // Show global extension
        System.out.println("Global TTL extension applied: +" + ttlService.getGlobalTTLExtension() + " days");
        
        // Remove global extension
        System.out.println("Removing global TTL extension...");
        ttlService.setGlobalTTLExtension(0);
        
        // Show final status
        System.out.println("Global TTL extension removed: +" + ttlService.getGlobalTTLExtension() + " days");
    }
    
    private void demonstrateStatusMonitoring(TTLManagementService ttlService) {
        System.out.println("\n📊 Demo 6: Status Monitoring");
        System.out.println("------------------------------");
        
        // Get comprehensive status
        TTLManagementStatus status = ttlService.getStatus();
        
        System.out.println("Comprehensive TTL Status:");
        System.out.println("=========================");
        System.out.println("TTL Management Enabled: " + status.isTTLManagementEnabled());
        System.out.println("Global TTL Override: " + status.isGlobalTTLEnabled());
        System.out.println("Global TTL Extension: +" + status.getGlobalTTLExtension() + " days");
        System.out.println("Total Operations: " + status.getTotalOperations());
        System.out.println("Scheduled Overrides: " + status.getScheduledOverridesCount());
        System.out.println("Auto Cleanup: " + status.isAutoCleanupEnabled());
        
        System.out.println("\nDetailed Overrides Summary:");
        System.out.println("===========================");
        System.out.println(status.getOverridesSummary());
    }
    
    private void printTTLStatus(TTLManagementStatus status) {
        System.out.println("  Global TTL Override: " + status.isGlobalTTLEnabled());
        System.out.println("  Global TTL Extension: +" + status.getGlobalTTLExtension() + " days");
        System.out.println("  Total Operations: " + status.getTotalOperations());
        System.out.println("  Scheduled Overrides: " + status.getScheduledOverridesCount());
    }
}

/**
 * Example service class with TTL annotations
 */
@LogTTL(start = "2025-01-01T00:00:00Z", ttlDays = 30, levels = {LogLevel.INFO, LogLevel.WARN})
class ExampleService {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(ExampleService.class);
    
    @LogTTL(ttlDays = 7, levels = {LogLevel.DEBUG})
    public void processData(String data) {
        logger.debug("Processing data: {}", data);
        logger.info("Data processing started");
        logger.warn("Data processing warning");
        logger.error("Data processing error");
    }
    
    public void performOperation() {
        logger.info("Operation performed");
        logger.debug("Operation details");
    }
}

/**
 * Example component that listens to TTL management events
 */
class TTLManagementDemoListener {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(TTLManagementDemoListener.class);
    
    public void onTTLEvent(String eventType, String message) {
        logger.info("TTL Event: {} - {}", eventType, message);
        
        switch (eventType) {
            case "GLOBAL_TTL_ENABLED":
                logger.warn("⚠️ Global TTL override enabled - all logs will work");
                break;
            case "GLOBAL_TTL_DISABLED":
                logger.info("✅ Global TTL override disabled - normal TTL rules restored");
                break;
            case "CLASS_TTL_OVERRIDDEN":
                logger.info("📝 Class TTL overridden - specific class logs modified");
                break;
            case "CLEANUP_EXECUTED":
                logger.debug("🧹 TTL cleanup executed - expired overrides removed");
                break;
        }
    }
}

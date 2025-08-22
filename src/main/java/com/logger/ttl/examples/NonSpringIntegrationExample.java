package com.logger.ttl.examples;

import com.logger.ttl.*;
import com.logger.ttl.integration.*;
import com.logger.ttl.integration.TTLEventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive example demonstrating how to use the TTL Integration Manager
 * in non-Spring Boot applications to build custom integrations and update
 * TTL configurations during runtime.
 */
@LogTTL(start = "2025-01-01T00:00:00Z", ttlDays = 30, levels = {LogLevel.DEBUG, LogLevel.INFO})
public class NonSpringIntegrationExample {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(NonSpringIntegrationExample.class);
    private static final TTLIntegrationManager integrationManager = TTLIntegrationManager.getInstance();
    
    // Custom integration components
    private final ScheduledExecutorService customScheduler;
    private final CustomTTLMonitor customMonitor;
    private final CustomTTLController customController;
    
    public NonSpringIntegrationExample() {
        this.customScheduler = Executors.newScheduledThreadPool(2);
        this.customMonitor = new CustomTTLMonitor();
        this.customController = new CustomTTLController();
        
        // Setup custom integrations
        setupCustomIntegrations();
    }
    
    /**
     * Setup custom integrations with the TTL system
     */
    private void setupCustomIntegrations() {
        // 1. Event-based integration
        integrationManager.addEventListener(customMonitor);
        
        // 2. Lambda-based event listener
        integrationManager.addLambdaEventListener(event -> {
            logger.info("Lambda event listener received: " + event.getEventType());
            if (event.getData() != null) {
                logger.debug("Event data: " + event.getData());
            }
        });
        
        // 3. Scheduled monitoring
        customScheduler.scheduleAtFixedRate(() -> {
            TTLManagementStatus status = integrationManager.getStatus();
            logger.debug("TTL Status: " + status);
        }, 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Demonstrate various integration patterns
     */
    public void demonstrateIntegrations() {
        logger.info("Starting TTL integration demonstrations");
        
        // 1. Event-driven TTL management
        demonstrateEventDrivenManagement();
        
        // 2. Custom controller integration
        demonstrateCustomController();
        
        // 3. Scheduled override management
        demonstrateScheduledOverrides();
        
        // 4. Custom monitoring integration
        demonstrateCustomMonitoring();
        
        // 5. Runtime TTL configuration updates
        demonstrateRuntimeUpdates();
        
        logger.info("TTL integration demonstrations completed");
    }
    
    /**
     * Demonstrate event-driven TTL management
     */
    private void demonstrateEventDrivenManagement() {
        logger.info("=== Event-Driven TTL Management ===");
        
        // Enable all TTL rules (will trigger events)
        integrationManager.enableAllTTL("Demonstration: Enable all TTL rules");
        
        // Extend global TTL (will trigger events)
        integrationManager.extendGlobalTTL(7, "Demonstration: Extend global TTL by 7 days");
        
        // Override class TTL (will trigger events)
        TTLOverride bypassOverride = TTLOverride.bypass();
        integrationManager.overrideClassTTL(PaymentService.class, bypassOverride, 
            "Demonstration: Bypass TTL for PaymentService");
        
        // Wait for events to be processed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Demonstrate custom controller integration
     */
    private void demonstrateCustomController() {
        logger.info("=== Custom Controller Integration ===");
        
        // Use custom controller to manage TTL
        customController.enableDebugLoggingForClass(UserService.class, 
            "Demonstration: Enable debug logging via custom controller");
        
        customController.scheduleTemporaryOverride("temp-override-1", OrderService.class, 
            TTLOverride.extend(14), 60000, "Demonstration: 1-minute temporary override");
        
        // Wait for scheduled override to be applied
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Demonstrate scheduled override management
     */
    private void demonstrateScheduledOverrides() {
        logger.info("=== Scheduled Override Management ===");
        
        // Schedule multiple overrides with different durations
        integrationManager.scheduleOverride("demo-override-1", PaymentService.class, 
            TTLOverride.bypass(), 5000, "Demonstration: 5-second override");
        
        integrationManager.scheduleOverride("demo-override-2", UserService.class, 
            TTLOverride.extend(30), 10000, "Demonstration: 10-second override");
        
        logger.info("Scheduled overrides: " + integrationManager.getScheduledOverrideIds());
        logger.info("Scheduled override count: " + integrationManager.getScheduledOverrideCount());
        
        // Wait for some overrides to expire
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("After expiration - Scheduled override count: " + integrationManager.getScheduledOverrideCount());
    }
    
    /**
     * Demonstrate custom monitoring integration
     */
    private void demonstrateCustomMonitoring() {
        logger.info("=== Custom Monitoring Integration ===");
        
        // Get current status
        TTLManagementStatus status = integrationManager.getStatus();
        logger.info("Current TTL Status: " + status);
        
        // Get operation count
        long totalOperations = integrationManager.getTotalOperations();
        logger.info("Total TTL operations: " + totalOperations);
        
        // Get event listener count
        int listenerCount = integrationManager.getEventListenerCount();
        logger.info("Active event listeners: " + listenerCount);
    }
    
    /**
     * Demonstrate runtime TTL configuration updates
     */
    private void demonstrateRuntimeUpdates() {
        logger.info("=== Runtime TTL Configuration Updates ===");
        
        // Update TTL configuration based on custom business logic
        if (shouldEnableDebugLogging()) {
            integrationManager.overrideClassTTL(PaymentService.class, 
                TTLOverride.bypass(), "Business logic: Enable debug logging for payments");
            logger.info("Debug logging enabled for PaymentService based on business logic");
        }
        
        // Dynamic TTL extension based on system load
        int extensionDays = calculateTTLExtension();
        if (extensionDays > 0) {
            integrationManager.extendGlobalTTL(extensionDays, 
                "System load: Extend TTL by " + extensionDays + " days");
            logger.info("TTL extended by " + extensionDays + " days based on system load");
        }
    }
    
    /**
     * Simulate business logic for enabling debug logging
     */
    private boolean shouldEnableDebugLogging() {
        // Simulate business logic (e.g., high error rate, customer request, etc.)
        return Math.random() > 0.7;
    }
    
    /**
     * Simulate system load calculation for TTL extension
     */
    private int calculateTTLExtension() {
        // Simulate system load calculation
        double load = Math.random();
        if (load > 0.8) return 7;      // High load: extend by 7 days
        else if (load > 0.6) return 3; // Medium load: extend by 3 days
        else return 0;                  // Low load: no extension
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        logger.info("Cleaning up TTL integration resources");
        
        // Clear all overrides
        integrationManager.clearAllOverrides("Cleanup: Remove all overrides");
        
        // Shutdown custom scheduler
        customScheduler.shutdown();
        
        // Remove event listeners
        integrationManager.removeEventListener(customMonitor);
        
        logger.info("TTL integration cleanup completed");
    }
    
    /**
     * Main method to run the example
     */
    public static void main(String[] args) {
        NonSpringIntegrationExample example = new NonSpringIntegrationExample();
        
        try {
            example.demonstrateIntegrations();
            
            // Keep running to see scheduled operations
            logger.info("Example completed. Keeping alive for 15 seconds to see scheduled operations...");
            Thread.sleep(15000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Example interrupted");
        } finally {
            example.cleanup();
        }
        
        // Shutdown integration manager
        integrationManager.shutdown();
        logger.info("TTL Integration Manager shutdown completed");
    }
    
    // ========================================
    // Custom Integration Components
    // ========================================
    
    /**
     * Custom TTL Monitor that implements TTLEventListener
     */
    private static class CustomTTLMonitor implements TTLEventListener {
        
        @Override
        public void onTTLEvent(TTLEvent event) {
            // Custom monitoring logic
            System.out.println("🔍 [Custom Monitor] TTL Event: " + event.getEventType());
            System.out.println("   Message: " + event.getMessage());
            System.out.println("   Reason: " + event.getReason());
            System.out.println("   Timestamp: " + event.getTimestamp());
            
            // Custom business logic based on event type
            switch (event.getEventType()) {
                case GLOBAL_TTL_ENABLED:
                    handleGlobalTTLEnabled(event);
                    break;
                case CLASS_TTL_OVERRIDDEN:
                    handleClassTTLOverridden(event);
                    break;
                case TTL_OVERRIDE_SCHEDULED:
                    handleTTLOverrideScheduled(event);
                    break;
                default:
                    // Handle other event types
                    break;
            }
        }
        
        private void handleGlobalTTLEnabled(TTLEvent event) {
            System.out.println("   🎯 [Custom Monitor] Global TTL enabled - updating monitoring rules");
            // Custom logic: Update monitoring rules, send notifications, etc.
        }
        
        private void handleClassTTLOverridden(TTLEvent event) {
            System.out.println("   🎯 [Custom Monitor] Class TTL overridden - updating class-specific monitoring");
            // Custom logic: Update class-specific monitoring, log to external systems, etc.
        }
        
        private void handleTTLOverrideScheduled(TTLEvent event) {
            System.out.println("   🎯 [Custom Monitor] TTL override scheduled - setting up temporary monitoring");
            // Custom logic: Set up temporary monitoring, schedule cleanup tasks, etc.
        }
    }
    
    /**
     * Custom TTL Controller for business-specific TTL management
     */
    private static class CustomTTLController {
        
        public void enableDebugLoggingForClass(Class<?> clazz, String reason) {
            TTLOverride debugOverride = TTLOverride.bypass();
            integrationManager.overrideClassTTL(clazz, debugOverride, reason);
            System.out.println("🎛️ [Custom Controller] Debug logging enabled for " + clazz.getSimpleName());
        }
        
        public void scheduleTemporaryOverride(String overrideId, Class<?> clazz, 
                                           TTLOverride override, long durationMs, String reason) {
            integrationManager.scheduleOverride(overrideId, clazz, override, durationMs, reason);
            System.out.println("🎛️ [Custom Controller] Temporary override scheduled for " + clazz.getSimpleName() + 
                             " (duration: " + durationMs + "ms)");
        }
        
        public void enableProductionDebugging(String reason) {
            // Business-specific logic: Enable debug logging for production troubleshooting
            integrationManager.enableAllTTL(reason);
            integrationManager.extendGlobalTTL(1, "Production debugging: Extend by 1 day");
            System.out.println("🎛️ [Custom Controller] Production debugging mode enabled");
        }
        
        public void enableComplianceAudit(String reason) {
            // Business-specific logic: Enable logging for compliance audit
            integrationManager.overrideClassTTL(PaymentService.class, TTLOverride.bypass(), reason);
            integrationManager.overrideClassTTL(UserService.class, TTLOverride.bypass(), reason);
            System.out.println("🎛️ [Custom Controller] Compliance audit mode enabled");
        }
    }
    
    // ========================================
    // Example Service Classes
    // ========================================
    
    @LogTTL(ttlDays = 7, levels = {LogLevel.DEBUG, LogLevel.INFO})
    private static class PaymentService {
        private static final TTLLogger logger = TTLLoggerFactory.getLogger(PaymentService.class);
        
        public void processPayment() {
            logger.debug("Processing payment (DEBUG - expires in 7 days)");
            logger.info("Payment processed successfully (INFO - expires in 7 days)");
            logger.warn("Payment warning (WARN - permanent)");
        }
    }
    
    @LogTTL(ttlDays = 14, levels = {LogLevel.DEBUG})
    private static class UserService {
        private static final TTLLogger logger = TTLLoggerFactory.getLogger(UserService.class);
        
        public void getUser() {
            logger.debug("Getting user (DEBUG - expires in 14 days)");
            logger.info("User retrieved (INFO - permanent)");
        }
    }
    
    @LogTTL(ttlDays = 30, levels = {LogLevel.INFO, LogLevel.WARN})
    private static class OrderService {
        private static final TTLLogger logger = TTLLoggerFactory.getLogger(OrderService.class);
        
        public void createOrder() {
            logger.debug("Creating order (DEBUG - permanent)");
            logger.info("Order created (INFO - expires in 30 days)");
            logger.warn("Order warning (WARN - expires in 30 days)");
        }
    }
}

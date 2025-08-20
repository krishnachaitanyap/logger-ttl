package com.logger.ttl.demo;

import com.logger.ttl.*;
import org.slf4j.Logger;

/**
 * Demonstrates runtime TTL management capabilities including:
 * - Re-enabling expired logs
 * - Extending TTL periods
 * - Overriding TTL configurations
 * - Global TTL control
 */
@LogTTL(start = "2025-01-01T00:00:00Z", ttlDays = 30, levels = {LogLevel.DEBUG, LogLevel.INFO})
public class RuntimeTTLManagementDemo {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(RuntimeTTLManagementDemo.class);
    
    @LogTTL(ttlDays = 7, levels = {LogLevel.DEBUG})
    public void demonstrateTTLExpiration() {
        logger.info("This INFO log expires in 30 days (class-level)");
        logger.debug("This DEBUG log expires in 7 days (method-level)");
        logger.warn("This WARN log expires in 30 days (class-level)");
        logger.error("This ERROR log expires in 30 days (class-level)");
    }
    
    public void demonstrateRuntimeOverrides() {
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("=== Runtime TTL Management Demo ===");
        logger.info("Current TTL status: " + manager.getOverridesSummary());
        
        // Scenario 1: Enable all TTL rules globally (bypass expiration)
        logger.info("Scenario 1: Enabling all TTL rules globally...");
        manager.enableAllTTL();
        
        // These logs should now work even if they were expired
        logger.debug("DEBUG log - should work even if expired (global override)");
        logger.info("INFO log - should work even if expired (global override)");
        
        // Scenario 2: Extend TTL globally by 15 days
        logger.info("Scenario 2: Extending all TTL by 15 days...");
        manager.disableAllTTL(); // Disable global override
        manager.setGlobalTTLExtension(15);
        
        logger.debug("DEBUG log - TTL extended by 15 days");
        logger.info("INFO log - TTL extended by 15 days");
        
        // Scenario 3: Override specific class TTL
        logger.info("Scenario 3: Overriding class TTL to never expire...");
        TTLOverride classOverride = TTLOverride.bypass();
        manager.overrideClassTTL(RuntimeTTLManagementDemo.class, classOverride);
        
        logger.debug("DEBUG log - class TTL overridden to never expire");
        logger.info("INFO log - class TTL overridden to never expire");
        
        // Scenario 4: Override specific method TTL
        logger.info("Scenario 4: Overriding method TTL to extend by 30 days...");
        TTLOverride methodOverride = TTLOverride.extend(30);
        manager.overrideMethodTTL(RuntimeTTLManagementDemo.class, "demonstrateTTLExpiration", methodOverride);
        
        // This method now has extended TTL
        demonstrateTTLExpiration();
        
        // Scenario 5: Replace TTL configuration completely
        logger.info("Scenario 5: Replacing TTL configuration...");
        TTLOverride replaceOverride = TTLOverride.replace("2025-12-31T23:59:59Z", 365, LogLevel.ERROR);
        manager.overrideClassTTL(RuntimeTTLManagementDemo.class, replaceOverride);
        
        logger.debug("DEBUG log - should not work (only ERROR level allowed)");
        logger.info("INFO log - should not work (only ERROR level allowed)");
        logger.error("ERROR log - should work with new TTL (365 days from 2025-12-31)");
        
        // Clean up overrides
        logger.info("Cleaning up all overrides...");
        manager.clearAllOverrides();
        
        logger.info("Final TTL status: " + manager.getOverridesSummary());
        logger.info("=== Demo Complete ===");
    }
    
    public void demonstrateProductionDebugging() {
        logger.info("=== Production Debugging Scenario ===");
        
        // Simulate a production issue where we need to enable expired DEBUG logs
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("Production issue detected - need to investigate with DEBUG logs");
        logger.info("Enabling expired DEBUG logs for investigation...");
        
        // Enable all TTL rules temporarily for debugging
        manager.enableAllTTL();
        
        // Now all logs work, including expired ones
        logger.debug("DEBUG log from expired TTL - now working for investigation");
        logger.debug("Another DEBUG log - useful for troubleshooting");
        logger.info("INFO log - also working due to global override");
        
        // After investigation, restore normal TTL behavior
        logger.info("Investigation complete - restoring normal TTL behavior");
        manager.disableAllTTL();
        
        logger.debug("DEBUG log - back to normal TTL rules");
        logger.info("INFO log - back to normal TTL rules");
        
        logger.info("=== Production Debugging Complete ===");
    }
    
    public void demonstrateComplianceAudit() {
        logger.info("=== Compliance Audit Scenario ===");
        
        TTLManager manager = TTLManager.getInstance();
        
        logger.info("Compliance audit requested - need to access logs from specific period");
        logger.info("Extending TTL by 90 days for audit purposes...");
        
        // Extend TTL globally for compliance audit
        manager.setGlobalTTLExtension(90);
        
        logger.info("INFO log - TTL extended by 90 days for audit");
        logger.debug("DEBUG log - TTL extended by 90 days for audit");
        logger.warn("WARN log - TTL extended by 90 days for audit");
        
        // After audit, remove extension
        logger.info("Audit complete - removing TTL extension");
        manager.setGlobalTTLExtension(0);
        
        logger.info("INFO log - back to original TTL");
        logger.debug("DEBUG log - back to original TTL");
        
        logger.info("=== Compliance Audit Complete ===");
    }
    
    public static void main(String[] args) {
        RuntimeTTLManagementDemo demo = new RuntimeTTLManagementDemo();
        
        // Run all demonstration scenarios
        demo.demonstrateRuntimeOverrides();
        demo.demonstrateProductionDebugging();
        demo.demonstrateComplianceAudit();
    }
}

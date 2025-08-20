package com.logger.ttl.examples;

import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

/**
 * Example service demonstrating field-level LogTTL annotation.
 * 
 * <p>This class shows how TTL can be applied to specific logger instances
 * rather than entire classes or methods.</p>
 */
public class OrderService {

    /**
     * Logger with TTL applied to DEBUG and INFO levels only.
     * These logs will expire in 15 days.
     */
    @LogTTL(ttlDays=15, levels={LogLevel.DEBUG, LogLevel.INFO})
    private static final TTLLogger debugLogger = TTLLoggerFactory.getLogger(OrderService.class);
    
    /**
     * Logger with TTL applied to all levels.
     * All logs will expire in 30 days.
     */
    @LogTTL(ttlDays=30)
    private static final TTLLogger generalLogger = TTLLoggerFactory.getLogger(OrderService.class);
    
    /**
     * Logger without TTL annotation.
     * All logs are permanent.
     */
    private static final TTLLogger permanentLogger = TTLLoggerFactory.getLogger(OrderService.class);

    public void processOrder() {
        // Using debugLogger - DEBUG and INFO expire in 15 days
        debugLogger.debug("Order processing started");      // ✅ expires in 15 days
        debugLogger.info("Order validated");               // ✅ expires in 15 days
        debugLogger.warn("Order processing slow");         // ❌ unaffected
        debugLogger.error("Order processing failed");       // ❌ unaffected
        
        // Using generalLogger - all levels expire in 30 days
        generalLogger.debug("Order details loaded");        // ✅ expires in 30 days
        generalLogger.info("Order processed successfully"); // ✅ expires in 30 days
        generalLogger.warn("Order took longer than expected"); // ✅ expires in 30 days
        generalLogger.error("Order processing error");      // ✅ expires in 30 days
        
        // Using permanentLogger - no TTL restrictions
        permanentLogger.debug("Order completed");           // ❌ unaffected
        permanentLogger.info("Order summary generated");    // ❌ unaffected
        permanentLogger.warn("Order archived");             // ❌ unaffected
        permanentLogger.error("Order cleanup completed");   // ❌ unaffected
    }
    
    public void cancelOrder() {
        // Mix of different loggers to show TTL behavior
        debugLogger.info("Order cancellation requested");   // ✅ expires in 15 days
        generalLogger.warn("Order cancellation in progress"); // ✅ expires in 30 days
        permanentLogger.error("Order cancellation completed"); // ❌ unaffected
    }
}

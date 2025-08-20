package com.logger.ttl.examples;

import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

/**
 * Example service demonstrating class-level LogTTL annotation.
 * 
 * <p>All logs in this class will respect the TTL rules:
 * - Start date: 2025-08-20T00:00:00Z
 * - TTL: 30 days
 * - Affected levels: DEBUG and INFO only
 * </p>
 */
@LogTTL(start="2025-08-20T00:00:00Z", ttlDays=30, levels={LogLevel.DEBUG, LogLevel.INFO})
public class PaymentService {

    private static final TTLLogger logger = TTLLoggerFactory.getLogger(PaymentService.class);

    public void processPayment() {
        logger.info("This INFO log expires in 30 days");   // ✅ expires
        logger.debug("This DEBUG log expires in 30 days"); // ✅ expires
        logger.error("This ERROR log is permanent");       // ❌ unaffected
        logger.warn("This WARN log is permanent");         // ❌ unaffected
    }
    
    public void validatePayment() {
        logger.debug("Payment validation started");         // ✅ expires
        logger.info("Payment validation completed");        // ✅ expires
        logger.error("Payment validation failed");          // ❌ unaffected
    }
    
    public void refundPayment() {
        logger.info("Processing refund");                   // ✅ expires
        logger.debug("Refund amount calculated");           // ✅ expires
        logger.warn("Refund may take 3-5 business days");  // ❌ unaffected
    }
}

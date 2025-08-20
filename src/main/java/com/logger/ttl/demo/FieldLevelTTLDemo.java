package com.logger.ttl.demo;

import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

/**
 * Demo class showing field-level LogTTL annotation usage.
 * 
 * <p>This class demonstrates how to apply TTL annotations to logger fields
 * to control expiration at the field level.</p>
 */
public class FieldLevelTTLDemo {
    
    /**
     * Logger with TTL applied to INFO and WARN levels only.
     * These logs will expire in 15 days.
     */
    @LogTTL(ttlDays=15, levels={LogLevel.INFO, LogLevel.WARN})
    private static final TTLLogger infoWarnLogger = TTLLoggerFactory.getLogger(FieldLevelTTLDemo.class);
    
    /**
     * Logger with TTL applied to all levels.
     * All logs will expire in 60 days.
     */
    @LogTTL(ttlDays=60)
    private static final TTLLogger allLevelsLogger = TTLLoggerFactory.getLogger(FieldLevelTTLDemo.class);
    
    /**
     * Logger without TTL annotation.
     * All logs are permanent.
     */
    private static final TTLLogger permanentLogger = TTLLoggerFactory.getLogger(FieldLevelTTLDemo.class);
    
    public static void runDemo() {
        System.out.println("--- Field-Level TTL Demo ---");
        
        System.out.println("  Info/Warn Logger (15 days, INFO/WARN only):");
        infoWarnLogger.debug("Field TTL DEBUG log - permanent (not affected)");
        infoWarnLogger.info("Field TTL INFO log - expires in 15 days");
        infoWarnLogger.warn("Field TTL WARN log - expires in 15 days");
        infoWarnLogger.error("Field TTL ERROR log - permanent (not affected)");
        
        System.out.println("  All Levels Logger (60 days, all levels):");
        allLevelsLogger.debug("All levels DEBUG log - expires in 60 days");
        allLevelsLogger.info("All levels INFO log - expires in 60 days");
        allLevelsLogger.warn("All levels WARN log - expires in 60 days");
        allLevelsLogger.error("All levels ERROR log - expires in 60 days");
        
        System.out.println("  Permanent Logger (no TTL):");
        permanentLogger.debug("Permanent DEBUG log - never expires");
        permanentLogger.info("Permanent INFO log - never expires");
        permanentLogger.warn("Permanent WARN log - never expires");
        permanentLogger.error("Permanent ERROR log - never expires");
        
        System.out.println("Field-level TTL demo completed.\n");
    }
}

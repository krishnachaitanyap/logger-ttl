package com.logger.ttl.examples;

import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;

/**
 * Example service demonstrating method-level LogTTL annotation.
 * 
 * <p>This class shows how different methods can have different TTL rules,
 * and how TTL can be applied to specific log levels only.</p>
 */
public class UserService {

    private static final TTLLogger logger = TTLLoggerFactory.getLogger(UserService.class);

    /**
     * Method with TTL applied to DEBUG level only.
     * DEBUG logs will expire in 7 days, other levels are unaffected.
     */
    @LogTTL(ttlDays=7, levels={LogLevel.DEBUG})
    public void getUser() {
        logger.debug("DEBUG log auto-expires in 7 days"); // ✅ expires
        logger.info("INFO log is permanent");             // ❌ unaffected
        logger.warn("WARN log is permanent");             // ❌ unaffected
        logger.error("ERROR log is permanent");           // ❌ unaffected
    }
    
    /**
     * Method with TTL applied to all levels.
     * All logs will expire in 14 days.
     */
    @LogTTL(ttlDays=14)
    public void createUser() {
        logger.debug("Creating new user");                 // ✅ expires
        logger.info("User created successfully");          // ✅ expires
        logger.warn("User creation took longer than expected"); // ✅ expires
        logger.error("Failed to create user");             // ✅ expires
    }
    
    /**
     * Method with start date and TTL.
     * Logs will only be valid from 2025-01-01 and expire in 60 days.
     */
    @LogTTL(start="2025-01-01T00:00:00Z", ttlDays=60, levels={LogLevel.INFO, LogLevel.WARN})
    public void updateUser() {
        logger.debug("DEBUG log is permanent");            // ❌ unaffected
        logger.info("INFO log expires in 60 days from 2025-01-01"); // ✅ expires
        logger.warn("WARN log expires in 60 days from 2025-01-01"); // ✅ expires
        logger.error("ERROR log is permanent");            // ❌ unaffected
    }
    
    /**
     * Method without TTL annotation.
     * All logs are permanent.
     */
    public void deleteUser() {
        logger.debug("User deletion started");             // ❌ unaffected
        logger.info("User deleted successfully");          // ❌ unaffected
        logger.warn("User deletion completed");            // ❌ unaffected
        logger.error("Failed to delete user");             // ❌ unaffected
    }
}

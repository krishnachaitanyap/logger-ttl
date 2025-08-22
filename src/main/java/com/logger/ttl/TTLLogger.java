package com.logger.ttl;

import com.logger.ttl.metrics.TTLMetricsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TTL-enabled logger that wraps SLF4J Logger and applies TTL rules
 * based on LogTTL annotations.
 * 
 * <p>This logger automatically inspects the calling context to determine
 * TTL configuration and skips log statements that have expired or are
 * not affected by TTL rules.</p>
 */
public class TTLLogger {
    
    private final Logger delegate;
    private final Class<?> loggerClass;
    
    /**
     * Creates a new TTL logger.
     * 
     * @param delegate the underlying SLF4J logger
     * @param loggerClass the class of the logger instance
     */
    TTLLogger(Logger delegate, Class<?> loggerClass) {
        this.delegate = delegate;
        this.loggerClass = loggerClass;
    }
    
    /**
     * Creates a new TTL logger for the specified class.
     * 
     * @param clazz the class to create a logger for
     * @return a new TTL logger instance
     */
    public static TTLLogger getLogger(Class<?> clazz) {
        Logger delegate = LoggerFactory.getLogger(clazz);
        return new TTLLogger(delegate, clazz);
    }
    
    /**
     * Creates a new TTL logger for the specified name.
     * 
     * @param name the name of the logger
     * @return a new TTL logger instance
     */
    public static TTLLogger getLogger(String name) {
        Logger delegate = LoggerFactory.getLogger(name);
        return new TTLLogger(delegate, null);
    }
    
    // TRACE level methods
    
    /**
     * Logs a TRACE level message with TTL checking.
     * 
     * @param msg the message to log
     */
    public void trace(String msg) {
        if (shouldLog(LogLevel.TRACE)) {
            delegate.trace(msg);
        }
    }
    
    /**
     * Logs a TRACE level message with explicit TTL configuration.
     * 
     * @param msg the message to log
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     */
    public void trace(String msg, String start, int ttlDays, LogLevel... levels) {
        TTLConfig config = new TTLConfig(start, ttlDays, levels);
        if (config.shouldLog(LogLevel.TRACE)) {
            delegate.trace(msg);
        }
    }
    
    // DEBUG level methods
    
    /**
     * Logs a DEBUG level message with TTL checking.
     * 
     * @param msg the message to log
     */
    public void debug(String msg) {
        if (shouldLog(LogLevel.DEBUG)) {
            delegate.debug(msg);
        }
    }
    
    /**
     * Logs a DEBUG level message with explicit TTL configuration.
     * 
     * @param msg the message to log
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     */
    public void debug(String msg, String start, int ttlDays, LogLevel... levels) {
        TTLConfig config = new TTLConfig(start, ttlDays, levels);
        if (config.shouldLog(LogLevel.DEBUG)) {
            delegate.debug(msg);
        }
    }
    
    // INFO level methods
    
    /**
     * Logs an INFO level message with TTL checking.
     * 
     * @param msg the message to log
     */
    public void info(String msg) {
        if (shouldLog(LogLevel.INFO)) {
            delegate.info(msg);
        }
    }
    
    /**
     * Logs an INFO level message with explicit TTL configuration.
     * 
     * @param msg the message to log
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     */
    public void info(String msg, String start, int ttlDays, LogLevel... levels) {
        TTLConfig config = new TTLConfig(start, ttlDays, levels);
        if (config.shouldLog(LogLevel.INFO)) {
            delegate.info(msg);
        }
    }
    
    // WARN level methods
    
    /**
     * Logs a WARN level message with TTL checking.
     * 
     * @param msg the message to log
     */
    public void warn(String msg) {
        if (shouldLog(LogLevel.WARN)) {
            delegate.warn(msg);
        }
    }
    
    /**
     * Logs a WARN level message with explicit TTL configuration.
     * 
     * @param msg the message to log
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     */
    public void warn(String msg, String start, int ttlDays, LogLevel... levels) {
        TTLConfig config = new TTLConfig(start, ttlDays, levels);
        if (config.shouldLog(LogLevel.WARN)) {
            delegate.warn(msg);
        }
    }
    
    // ERROR level methods
    
    /**
     * Logs an ERROR level message with TTL checking.
     * 
     * @param msg the message to log
     */
    public void error(String msg) {
        if (shouldLog(LogLevel.ERROR)) {
            delegate.error(msg);
        }
    }
    
    /**
     * Logs an ERROR level message with explicit TTL configuration.
     * 
     * @param msg the message to log
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     */
    public void error(String msg, String start, int ttlDays, LogLevel... levels) {
        TTLConfig config = new TTLConfig(start, ttlDays, levels);
        if (config.shouldLog(LogLevel.ERROR)) {
            delegate.error(msg);
        }
    }
    
    // Utility methods
    
    /**
     * Checks if the specified log level is enabled.
     * 
     * @param level the log level to check
     * @return true if the level is enabled
     */
    public boolean isEnabledFor(LogLevel level) {
        switch (level) {
            case TRACE: return delegate.isTraceEnabled();
            case DEBUG: return delegate.isDebugEnabled();
            case INFO: return delegate.isInfoEnabled();
            case WARN: return delegate.isWarnEnabled();
            case ERROR: return delegate.isErrorEnabled();
            default: return false;
        }
    }
    
    /**
     * Gets the underlying SLF4J logger.
     * 
     * @return the underlying logger
     */
    public Logger getDelegate() {
        return delegate;
    }
    
    /**
     * Determines if a log statement should be executed based on TTL rules.
     * 
     * @param level the log level
     * @return true if the log should be executed
     */
    private boolean shouldLog(LogLevel level) {
        if (loggerClass == null) {
            // If we can't determine the logger class, always log
            return true;
        }
        
        TTLConfig config = TTLAnnotationProcessor.getTTLConfig(loggerClass, level);
        boolean shouldLog = config.shouldLog(level);
        
        // Record metrics
        try {
            TTLMetricsManager metricsManager = TTLMetricsManager.getInstance();
            if (shouldLog) {
                metricsManager.getMetrics().recordActiveLog(level);
            } else {
                metricsManager.getMetrics().recordExpiredLog(level);
            }
        } catch (Exception e) {
            // Don't let metrics errors interfere with logging
            // This could happen if metrics are not properly configured
        }
        
        return shouldLog;
    }
}

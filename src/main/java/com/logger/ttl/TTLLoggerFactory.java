package com.logger.ttl;

/**
 * Factory class for creating TTL-enabled loggers.
 * 
 * <p>This class serves as a drop-in replacement for SLF4J LoggerFactory,
 * providing TTL-enabled logging capabilities while maintaining the same
 * API surface.</p>
 * 
 * <p>Usage:</p>
 * <pre>
 * // Replace this:
 * Logger logger = LoggerFactory.getLogger(MyClass.class);
 * 
 * // With this:
 * TTLLogger logger = TTLLoggerFactory.getLogger(MyClass.class);
 * </pre>
 */
public class TTLLoggerFactory {
    
    /**
     * Creates a TTL logger for the specified class.
     * 
     * @param clazz the class to create a logger for
     * @return a TTL logger instance
     */
    public static TTLLogger getLogger(Class<?> clazz) {
        return TTLLogger.getLogger(clazz);
    }
    
    /**
     * Creates a TTL logger for the specified name.
     * 
     * @param name the name of the logger
     * @return a TTL logger instance
     */
    public static TTLLogger getLogger(String name) {
        return TTLLogger.getLogger(name);
    }
    
    /**
     * Creates a TTL logger for the specified class with explicit TTL configuration.
     * 
     * @param clazz the class to create a logger for
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     * @return a TTL logger instance
     */
    public static TTLLogger getLogger(Class<?> clazz, String start, int ttlDays, LogLevel... levels) {
        return new TTLLoggerWithConfig(clazz, start, ttlDays, levels);
    }
    
    /**
     * Creates a TTL logger for the specified name with explicit TTL configuration.
     * 
     * @param name the name of the logger
     * @param start ISO8601 start date
     * @param ttlDays TTL in days
     * @param levels affected log levels
     * @return a TTL logger instance
     */
    public static TTLLogger getLogger(String name, String start, int ttlDays, LogLevel... levels) {
        return new TTLLoggerWithConfig(name, start, ttlDays, levels);
    }
    
    /**
     * Private implementation of TTLLogger that uses explicit TTL configuration
     * instead of annotation-based configuration.
     */
    private static class TTLLoggerWithConfig extends TTLLogger {
        
        private final TTLConfig explicitConfig;
        
        public TTLLoggerWithConfig(Class<?> clazz, String start, int ttlDays, LogLevel... levels) {
            super(org.slf4j.LoggerFactory.getLogger(clazz), clazz);
            this.explicitConfig = new TTLConfig(start, ttlDays, levels);
        }
        
        public TTLLoggerWithConfig(String name, String start, int ttlDays, LogLevel... levels) {
            super(org.slf4j.LoggerFactory.getLogger(name), null);
            this.explicitConfig = new TTLConfig(start, ttlDays, levels);
        }
        
        @Override
        public void trace(String msg) {
            if (explicitConfig.shouldLog(LogLevel.TRACE)) {
                super.trace(msg);
            }
        }
        
        @Override
        public void debug(String msg) {
            if (explicitConfig.shouldLog(LogLevel.DEBUG)) {
                super.debug(msg);
            }
        }
        
        @Override
        public void info(String msg) {
            if (explicitConfig.shouldLog(LogLevel.INFO)) {
                super.info(msg);
            }
        }
        
        @Override
        public void warn(String msg) {
            if (explicitConfig.shouldLog(LogLevel.WARN)) {
                super.warn(msg);
            }
        }
        
        @Override
        public void error(String msg) {
            if (explicitConfig.shouldLog(LogLevel.ERROR)) {
                super.error(msg);
            }
        }
    }
}

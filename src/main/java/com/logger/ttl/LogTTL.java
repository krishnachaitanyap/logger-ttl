package com.logger.ttl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring Time-To-Live (TTL) logging behavior.
 * 
 * <p>This annotation can be applied at class, method, or field level to control
 * when log statements expire and which log levels are affected.</p>
 * 
 * <p>Usage examples:</p>
 * <ul>
 *   <li>Class-level: All logs in the class respect TTL rules</li>
 *   <li>Method-level: All logs in the method respect TTL rules</li>
 *   <li>Field-level: Only that specific logger instance respects TTL rules</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface LogTTL {
    
    /**
     * ISO8601 start date string (e.g., "2025-08-20T00:00:00Z").
     * If empty or not specified, no start restriction is applied.
     * 
     * @return the start date string
     */
    String start() default "";
    
    /**
     * Number of days until the log expires.
     * -1 means never expires (default).
     * 
     * @return the TTL in days
     */
    int ttlDays() default -1;
    
    /**
     * Log levels to which TTL rules apply.
     * If empty, TTL applies to all levels.
     * 
     * @return array of log levels
     */
    LogLevel[] levels() default {};
}

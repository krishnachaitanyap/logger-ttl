package com.logger.ttl;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for TTL logging settings.
 * 
 * <p>This class encapsulates the TTL parameters and provides validation
 * and utility methods for checking if logs should expire.</p>
 */
public class TTLConfig {
    
    private final Instant startTime;
    private final int ttlDays;
    private final Set<LogLevel> levels;
    private final boolean hasStartTime;
    private final boolean hasTTL;
    private final Instant creationTime;
    
    /**
     * Creates a new TTL configuration.
     * 
     * @param start ISO8601 start date string, or empty string for no start restriction
     * @param ttlDays number of days until expiration, or -1 for never expires
     * @param levels log levels to which TTL applies, or empty array for all levels
     */
    public TTLConfig(String start, int ttlDays, LogLevel[] levels) {
        this.ttlDays = ttlDays;
        this.hasTTL = ttlDays > 0;
        
        // Initialize start time and hasStartTime
        Instant startTimeTemp = null;
        boolean hasStartTimeTemp = false;
        
        if (start != null && !start.trim().isEmpty()) {
            try {
                startTimeTemp = Instant.parse(start.trim());
                hasStartTimeTemp = true;
            } catch (DateTimeParseException e) {
                // Gracefully handle invalid dates by treating them as no start restriction
                startTimeTemp = null;
                hasStartTimeTemp = false;
            }
        }
        
        this.startTime = startTimeTemp;
        this.hasStartTime = hasStartTimeTemp;
        this.creationTime = Instant.now();
        
        if (levels != null && levels.length > 0) {
            this.levels = new HashSet<>(Arrays.asList(levels));
        } else {
            this.levels = new HashSet<>();
        }
    }
    
    /**
     * Creates a default TTL configuration (no restrictions).
     * 
     * @return a default TTL configuration
     */
    public static TTLConfig defaultConfig() {
        return new TTLConfig("", -1, new LogLevel[0]);
    }
    
    /**
     * Checks if the current time is within the valid TTL window.
     * 
     * @return true if the log should be executed, false if it should be skipped
     */
    public boolean isWithinTTL() {
        // If no TTL restrictions, always allow
        if (!hasStartTime && !hasTTL) {
            return true;
        }
        
        Instant now = Instant.now();
        
        // Check start time restriction
        if (hasStartTime && now.isBefore(startTime)) {
            return false;
        }
        
        // Check TTL expiration
        if (hasTTL) {
            Instant expirationTime;
            if (startTime != null) {
                // If start time is specified, calculate expiration from start time
                expirationTime = startTime.plusSeconds(ttlDays * 24 * 60 * 60);
            } else {
                // If no start time, calculate expiration from creation time
                expirationTime = creationTime.plusSeconds(ttlDays * 24 * 60 * 60);
            }
            
            if (now.isAfter(expirationTime)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the specified log level is affected by TTL rules.
     * 
     * @param level the log level to check
     * @return true if the level is affected by TTL, false otherwise
     */
    public boolean isLevelAffected(LogLevel level) {
        // If no specific levels specified, all levels are affected
        if (levels.isEmpty()) {
            return true;
        }
        
        return levels.contains(level);
    }
    
    /**
     * Checks if a log statement should be executed based on TTL rules and log level.
     * 
     * @param level the log level of the statement
     * @return true if the log should be executed, false if it should be skipped
     */
    public boolean shouldLog(LogLevel level) {
        // If level is not affected by TTL, always allow
        if (!isLevelAffected(level)) {
            return true;
        }
        
        // Check TTL restrictions
        return isWithinTTL();
    }
    
    /**
     * Gets the start time.
     * 
     * @return the start time, or null if not specified
     */
    public Instant getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the TTL in days.
     * 
     * @return the TTL in days, or -1 if never expires
     */
    public int getTtlDays() {
        return ttlDays;
    }
    
    /**
     * Gets the affected log levels.
     * 
     * @return set of affected log levels, or empty set if all levels are affected
     */
    public Set<LogLevel> getLevels() {
        return new HashSet<>(levels);
    }
    
    /**
     * Checks if this configuration has start time restrictions.
     * 
     * @return true if start time is specified
     */
    public boolean hasStartTime() {
        return hasStartTime;
    }
    
    /**
     * Checks if this configuration has TTL restrictions.
     * 
     * @return true if TTL is specified
     */
    public boolean hasTTL() {
        return hasTTL;
    }
    
    @Override
    public String toString() {
        return "TTLConfig{" +
                "startTime=" + startTime +
                ", ttlDays=" + ttlDays +
                ", levels=" + levels +
                '}';
    }
}

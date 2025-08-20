package com.logger.ttl;

import java.time.Instant;
import java.util.Set;

/**
 * Defines override rules for TTL configurations, allowing runtime modification
 * of TTL behavior without changing the original annotations.
 */
public class TTLOverride {
    
    public enum OverrideType {
        /** Bypass TTL completely - always log */
        BYPASS,
        /** Extend TTL by additional days */
        EXTEND,
        /** Override with new TTL configuration */
        REPLACE
    }
    
    private final OverrideType type;
    private final int extraDays;
    private final String newStartDate;
    private final int newTtlDays;
    private final LogLevel[] newLevels;
    private final Instant overrideTime;
    
    /**
     * Create a bypass override (always log regardless of TTL)
     */
    public static TTLOverride bypass() {
        return new TTLOverride(OverrideType.BYPASS, 0, null, -1, null);
    }
    
    /**
     * Create an extension override (add extra days to existing TTL)
     */
    public static TTLOverride extend(int extraDays) {
        return new TTLOverride(OverrideType.EXTEND, extraDays, null, -1, null);
    }
    
    /**
     * Create a replacement override (completely new TTL configuration)
     */
    public static TTLOverride replace(String startDate, int ttlDays, LogLevel... levels) {
        return new TTLOverride(OverrideType.REPLACE, 0, startDate, ttlDays, levels);
    }
    
    private TTLOverride(OverrideType type, int extraDays, String newStartDate, int newTtlDays, LogLevel[] newLevels) {
        this.type = type;
        this.extraDays = extraDays;
        this.newStartDate = newStartDate;
        this.newTtlDays = newTtlDays;
        this.newLevels = newLevels;
        this.overrideTime = Instant.now();
    }
    
    /**
     * Check if this override should bypass the original TTL configuration
     */
    public boolean shouldBypass(TTLConfig originalConfig) {
        switch (type) {
            case BYPASS:
                return true;
            case EXTEND:
                // For extension, we don't bypass - we modify the original
                return false;
            case REPLACE:
                // For replacement, we don't bypass - we use new config
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Apply this override to an original TTL configuration
     */
    public TTLConfig apply(TTLConfig originalConfig) {
        switch (type) {
            case BYPASS:
                // Return a config that never expires
                return new TTLConfig("", -1, new LogLevel[0]);
                
            case EXTEND:
                // Extend the original TTL by extra days
                String startDate = originalConfig.getStartTime() != null ? 
                    originalConfig.getStartTime().toString() : "";
                return new TTLConfig(
                    startDate,
                    originalConfig.getTtlDays() + extraDays,
                    originalConfig.getLevels().toArray(new LogLevel[0])
                );
                
            case REPLACE:
                // Use completely new configuration
                return new TTLConfig(newStartDate, newTtlDays, newLevels);
                
            default:
                return originalConfig;
        }
    }
    
    /**
     * Get the override type
     */
    public OverrideType getType() {
        return type;
    }
    
    /**
     * Get extra days for extension overrides
     */
    public int getExtraDays() {
        return extraDays;
    }
    
    /**
     * Get new start date for replacement overrides
     */
    public String getNewStartDate() {
        return newStartDate;
    }
    
    /**
     * Get new TTL days for replacement overrides
     */
    public int getNewTtlDays() {
        return newTtlDays;
    }
    
    /**
     * Get new log levels for replacement overrides
     */
    public LogLevel[] getNewLevels() {
        return newLevels != null ? newLevels.clone() : new LogLevel[0];
    }
    
    /**
     * Get when this override was created
     */
    public Instant getOverrideTime() {
        return overrideTime;
    }
    
    @Override
    public String toString() {
        switch (type) {
            case BYPASS:
                return "TTLOverride{type=BYPASS, overrideTime=" + overrideTime + "}";
            case EXTEND:
                return "TTLOverride{type=EXTEND, extraDays=" + extraDays + ", overrideTime=" + overrideTime + "}";
            case REPLACE:
                return "TTLOverride{type=REPLACE, startDate='" + newStartDate + "', ttlDays=" + newTtlDays + 
                       ", levels=" + (newLevels != null ? newLevels.length : 0) + ", overrideTime=" + overrideTime + "}";
            default:
                return "TTLOverride{type=" + type + ", overrideTime=" + overrideTime + "}";
        }
    }
}

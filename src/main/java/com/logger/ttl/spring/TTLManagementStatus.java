package com.logger.ttl.spring;

/**
 * Status information for TTL management in Spring Boot applications.
 * Provides comprehensive information about current TTL management state.
 */
public class TTLManagementStatus {
    
    private final boolean globalTTLEnabled;
    private final int globalTTLExtension;
    private final String overridesSummary;
    private final long totalOperations;
    private final int scheduledOverridesCount;
    private final boolean ttlManagementEnabled;
    private final boolean autoCleanupEnabled;
    
    public TTLManagementStatus(boolean globalTTLEnabled, int globalTTLExtension, 
                              String overridesSummary, long totalOperations, 
                              int scheduledOverridesCount, boolean ttlManagementEnabled, 
                              boolean autoCleanupEnabled) {
        this.globalTTLEnabled = globalTTLEnabled;
        this.globalTTLExtension = globalTTLExtension;
        this.overridesSummary = overridesSummary;
        this.totalOperations = totalOperations;
        this.scheduledOverridesCount = scheduledOverridesCount;
        this.ttlManagementEnabled = ttlManagementEnabled;
        this.autoCleanupEnabled = autoCleanupEnabled;
    }
    
    public boolean isGlobalTTLEnabled() {
        return globalTTLEnabled;
    }
    
    public int getGlobalTTLExtension() {
        return globalTTLExtension;
    }
    
    public String getOverridesSummary() {
        return overridesSummary;
    }
    
    public long getTotalOperations() {
        return totalOperations;
    }
    
    public int getScheduledOverridesCount() {
        return scheduledOverridesCount;
    }
    
    public boolean isTTLManagementEnabled() {
        return ttlManagementEnabled;
    }
    
    public boolean isAutoCleanupEnabled() {
        return autoCleanupEnabled;
    }
    
    @Override
    public String toString() {
        return "TTLManagementStatus{" +
                "globalTTLEnabled=" + globalTTLEnabled +
                ", globalTTLExtension=" + globalTTLExtension +
                ", overridesSummary='" + overridesSummary + '\'' +
                ", totalOperations=" + totalOperations +
                ", scheduledOverridesCount=" + scheduledOverridesCount +
                ", ttlManagementEnabled=" + ttlManagementEnabled +
                ", autoCleanupEnabled=" + autoCleanupEnabled +
                '}';
    }
}

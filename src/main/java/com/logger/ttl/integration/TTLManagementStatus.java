package com.logger.ttl.integration;

/**
 * Status information for TTL management in non-Spring applications.
 * Provides comprehensive information about current TTL management state.
 */
public class TTLManagementStatus {
    
    private final boolean globalTTLEnabled;
    private final int globalTTLExtension;
    private final String overridesSummary;
    private final int listenerCount;
    private final boolean eventPublishingEnabled;
    
    public TTLManagementStatus(boolean globalTTLEnabled, int globalTTLExtension, 
                              String overridesSummary, int listenerCount, 
                              boolean eventPublishingEnabled) {
        this.globalTTLEnabled = globalTTLEnabled;
        this.globalTTLExtension = globalTTLExtension;
        this.overridesSummary = overridesSummary;
        this.listenerCount = listenerCount;
        this.eventPublishingEnabled = eventPublishingEnabled;
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
    
    public int getListenerCount() {
        return listenerCount;
    }
    
    public boolean isEventPublishingEnabled() {
        return eventPublishingEnabled;
    }
    
    @Override
    public String toString() {
        return "TTLManagementStatus{" +
                "globalTTLEnabled=" + globalTTLEnabled +
                ", globalTTLExtension=" + globalTTLExtension +
                ", overridesSummary='" + overridesSummary + '\'' +
                ", listenerCount=" + listenerCount +
                ", eventPublishingEnabled=" + eventPublishingEnabled +
                '}';
    }
}

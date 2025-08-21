package com.logger.ttl.integration;

/**
 * Interface for listening to TTL management events in non-Spring applications.
 * Implement this interface to build custom integrations that can update TTL configs during runtime.
 */
@FunctionalInterface
public interface TTLEventListener {
    
    /**
     * Called when a TTL management event occurs.
     * 
     * @param event the TTL management event
     */
    void onTTLEvent(TTLEvent event);
}

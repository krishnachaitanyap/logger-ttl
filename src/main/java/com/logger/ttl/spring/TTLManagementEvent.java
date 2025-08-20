package com.logger.ttl.spring;

import java.time.Instant;

/**
 * Event class for TTL management operations in Spring Boot applications.
 * Allows other components to listen to TTL management events.
 */
public class TTLManagementEvent {
    
    public enum EventType {
        SERVICE_STARTED,
        GLOBAL_TTL_ENABLED,
        GLOBAL_TTL_DISABLED,
        GLOBAL_TTL_EXTENDED,
        CLASS_TTL_OVERRIDDEN,
        METHOD_TTL_OVERRIDDEN,
        FIELD_TTL_OVERRIDDEN,
        TTL_OVERRIDE_SCHEDULED,
        TTL_OVERRIDE_REMOVED,
        ALL_OVERRIDES_CLEARED,
        CLEANUP_EXECUTED
    }
    
    private final String message;
    private final EventType eventType;
    private final String reason;
    private final Instant timestamp;
    
    public TTLManagementEvent(String message, EventType eventType, String reason) {
        this.message = message;
        this.eventType = eventType;
        this.reason = reason;
        this.timestamp = Instant.now();
    }
    
    public String getMessage() {
        return message;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "TTLManagementEvent{" +
                "message='" + message + '\'' +
                ", eventType=" + eventType +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

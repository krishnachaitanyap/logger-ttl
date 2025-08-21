package com.logger.ttl.integration;

/**
 * Event class for TTL management operations in non-Spring applications.
 * Allows custom integrations to listen to TTL management events.
 */
public class TTLEvent {
    
    private final TTLEventType eventType;
    private final String message;
    private final String reason;
    private final Object data;
    private final long timestamp;
    
    public TTLEvent(TTLEventType eventType, String message, String reason, Object data, long timestamp) {
        this.eventType = eventType;
        this.message = message;
        this.reason = reason;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    public TTLEventType getEventType() {
        return eventType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getReason() {
        return reason;
    }
    
    public Object getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "TTLEvent{" +
                "eventType=" + eventType +
                ", message='" + message + '\'' +
                ", reason='" + reason + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}

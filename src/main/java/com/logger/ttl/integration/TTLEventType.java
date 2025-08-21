package com.logger.ttl.integration;

/**
 * Enum defining the types of TTL management events that can be published.
 */
public enum TTLEventType {
    
    /** TTL management system started */
    SYSTEM_STARTED,
    
    /** All TTL rules enabled globally */
    GLOBAL_TTL_ENABLED,
    
    /** All TTL rules disabled globally */
    GLOBAL_TTL_DISABLED,
    
    /** Global TTL extended by additional days */
    GLOBAL_TTL_EXTENDED,
    
    /** Class-level TTL overridden */
    CLASS_TTL_OVERRIDDEN,
    
    /** Method-level TTL overridden */
    METHOD_TTL_OVERRIDDEN,
    
    /** Field-level TTL overridden */
    FIELD_TTL_OVERRIDDEN,
    
    /** TTL override scheduled for automatic removal */
    TTL_OVERRIDE_SCHEDULED,
    
    /** TTL override automatically removed */
    TTL_OVERRIDE_REMOVED,
    
    /** All TTL overrides cleared */
    ALL_OVERRIDES_CLEARED,
    
    /** Scheduled cleanup of TTL overrides executed */
    CLEANUP_EXECUTED,
    
    /** TTL management event publishing enabled */
    EVENT_PUBLISHING_ENABLED,
    
    /** TTL management event publishing disabled */
    EVENT_PUBLISHING_DISABLED,
    
    /** Custom TTL management event */
    CUSTOM_EVENT
}

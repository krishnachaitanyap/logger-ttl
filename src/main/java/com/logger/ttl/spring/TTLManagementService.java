package com.logger.ttl.spring;

import com.logger.ttl.*;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Spring Boot service for managing TTL rules with Spring-specific features.
 * Provides REST-like operations, scheduled cleanup, and event-driven TTL management.
 */
@Service
public class TTLManagementService {
    
    private final TTLManager ttlManager;
    private final ApplicationEventPublisher eventPublisher;
    
    // Track TTL management operations for monitoring
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final Map<String, TTLOverride> scheduledOverrides = new ConcurrentHashMap<>();
    
    @Value("${ttl.management.enabled:true}")
    private boolean ttlManagementEnabled;
    
    @Value("${ttl.management.auto-cleanup:true}")
    private boolean autoCleanupEnabled;
    
    @Value("${ttl.management.cleanup-interval:300000}") // 5 minutes default
    private long cleanupIntervalMs;
    
    public TTLManagementService(ApplicationEventPublisher eventPublisher) {
        this.ttlManager = TTLManager.getInstance();
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Enable TTL management when Spring Boot application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (ttlManagementEnabled) {
            publishEvent(new TTLManagementEvent("TTL Management Service Started", 
                TTLManagementEvent.EventType.SERVICE_STARTED, null));
        }
    }
    
    /**
     * Enable all TTL rules globally (bypass all expiration)
     */
    public void enableAllTTL(String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.enableAllTTL();
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "All TTL rules enabled: " + reason,
            TTLManagementEvent.EventType.GLOBAL_TTL_ENABLED,
            reason
        ));
    }
    
    /**
     * Disable all TTL rules globally (enforce all expiration)
     */
    public void disableAllTTL(String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.disableAllTTL();
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "All TTL rules disabled: " + reason,
            TTLManagementEvent.EventType.GLOBAL_TTL_DISABLED,
            reason
        ));
    }
    
    /**
     * Extend all TTL globally by specified days
     */
    public void extendGlobalTTL(int extraDays, String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.setGlobalTTLExtension(extraDays);
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "Global TTL extended by " + extraDays + " days: " + reason,
            TTLManagementEvent.EventType.GLOBAL_TTL_EXTENDED,
            reason
        ));
    }
    
    /**
     * Override TTL for a specific class
     */
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.overrideClassTTL(clazz, override);
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "Class TTL overridden for " + clazz.getSimpleName() + ": " + reason,
            TTLManagementEvent.EventType.CLASS_TTL_OVERRIDDEN,
            reason
        ));
    }
    
    /**
     * Override TTL for a specific method
     */
    public void overrideMethodTTL(Class<?> clazz, String methodName, TTLOverride override, String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.overrideMethodTTL(clazz, methodName, override);
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "Method TTL overridden for " + clazz.getSimpleName() + "#" + methodName + ": " + reason,
            TTLManagementEvent.EventType.METHOD_TTL_OVERRIDDEN,
            reason
        ));
    }
    
    /**
     * Override TTL for a specific field
     */
    public void overrideFieldTTL(Class<?> clazz, String fieldName, TTLOverride override, String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.overrideFieldTTL(clazz, fieldName, override);
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "Field TTL overridden for " + clazz.getSimpleName() + "." + fieldName + ": " + reason,
            TTLManagementEvent.EventType.FIELD_TTL_OVERRIDDEN,
            reason
        ));
    }
    
    /**
     * Schedule a TTL override that will be automatically removed after specified duration
     */
    public void scheduleTTLOverride(String overrideId, Class<?> clazz, TTLOverride override, 
                                   long durationMs, String reason) {
        if (!ttlManagementEnabled) return;
        
        // Apply the override
        ttlManager.overrideClassTTL(clazz, override);
        
        // Schedule removal
        scheduledOverrides.put(overrideId, override);
        
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "Scheduled TTL override for " + clazz.getSimpleName() + " (ID: " + overrideId + "): " + reason,
            TTLManagementEvent.EventType.TTL_OVERRIDE_SCHEDULED,
            reason
        ));
        
        // Schedule removal after duration
        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
                removeScheduledOverride(overrideId, clazz, "Scheduled removal");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Remove a scheduled TTL override
     */
    public void removeScheduledOverride(String overrideId, Class<?> clazz, String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.removeClassTTLOverride(clazz);
        scheduledOverrides.remove(overrideId);
        
        publishEvent(new TTLManagementEvent(
            "Scheduled TTL override removed for " + clazz.getSimpleName() + " (ID: " + overrideId + "): " + reason,
            TTLManagementEvent.EventType.TTL_OVERRIDE_REMOVED,
            reason
        ));
    }
    
    /**
     * Clear all TTL overrides
     */
    public void clearAllOverrides(String reason) {
        if (!ttlManagementEnabled) return;
        
        ttlManager.clearAllOverrides();
        scheduledOverrides.clear();
        totalOperations.incrementAndGet();
        
        publishEvent(new TTLManagementEvent(
            "All TTL overrides cleared: " + reason,
            TTLManagementEvent.EventType.ALL_OVERRIDES_CLEARED,
            reason
        ));
    }
    
    /**
     * Get current TTL management status
     */
    public TTLManagementStatus getStatus() {
        return new TTLManagementStatus(
            ttlManager.isGlobalTTLEnabled(),
            ttlManager.getGlobalTTLExtension(),
            ttlManager.getOverridesSummary(),
            totalOperations.get(),
            scheduledOverrides.size(),
            ttlManagementEnabled,
            autoCleanupEnabled
        );
    }
    
    /**
     * Get TTL override for a specific class
     */
    public TTLOverride getClassTTLOverride(Class<?> clazz) {
        return ttlManager.getClassTTLOverride(clazz);
    }
    
    /**
     * Get TTL override for a specific method
     */
    public TTLOverride getMethodTTLOverride(Class<?> clazz, String methodName) {
        return ttlManager.getMethodTTLOverride(clazz, methodName);
    }
    
    /**
     * Get TTL override for a specific field
     */
    public TTLOverride getFieldTTLOverride(Class<?> clazz, String fieldName) {
        return ttlManager.getFieldTTLOverride(clazz, fieldName);
    }
    
    /**
     * Check if TTL should be bypassed for specific context
     */
    public boolean shouldBypassTTL(Class<?> clazz, String methodName, String fieldName, TTLConfig config) {
        return ttlManager.shouldBypassTTL(clazz, methodName, fieldName, config);
    }
    
    /**
     * Apply global TTL extension to TTL configuration
     */
    public TTLConfig applyGlobalExtension(TTLConfig config) {
        return ttlManager.applyGlobalExtension(config);
    }
    
    /**
     * Scheduled cleanup of expired overrides
     */
    @Scheduled(fixedDelayString = "${ttl.management.cleanup-interval:300000}")
    public void cleanupExpiredOverrides() {
        if (!autoCleanupEnabled || !ttlManagementEnabled) return;
        
        // This could be extended to check for time-based expiration
        // For now, we just log the cleanup operation
        publishEvent(new TTLManagementEvent(
            "Scheduled cleanup of TTL overrides",
            TTLManagementEvent.EventType.CLEANUP_EXECUTED,
            null
        ));
    }
    
    /**
     * Publish TTL management events
     */
    private void publishEvent(TTLManagementEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }
    
    /**
     * Check if TTL management is enabled
     */
    public boolean isTTLManagementEnabled() {
        return ttlManagementEnabled;
    }
    
    /**
     * Get total operations count
     */
    public long getTotalOperations() {
        return totalOperations.get();
    }
    
    /**
     * Get scheduled overrides count
     */
    public int getScheduledOverridesCount() {
        return scheduledOverrides.size();
    }
}

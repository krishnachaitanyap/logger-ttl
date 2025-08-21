package com.logger.ttl.integration;

import com.logger.ttl.TTLManager;
import com.logger.ttl.TTLOverride;
import com.logger.ttl.TTLConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Comprehensive integration manager for TTL management that works without Spring Boot.
 * Provides multiple integration patterns for non-Spring applications to build custom integrations
 * and update TTL configurations during runtime.
 */
public class TTLIntegrationManager {
    
    private static final TTLIntegrationManager INSTANCE = new TTLIntegrationManager();
    
    private final TTLManager ttlManager;
    private final TTLEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<String, ScheduledOverride> scheduledOverrides;
    private final AtomicLong operationCounter;
    
    private TTLIntegrationManager() {
        this.ttlManager = TTLManager.getInstance();
        this.eventPublisher = TTLEventPublisher.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.scheduledOverrides = new ConcurrentHashMap<>();
        this.operationCounter = new AtomicLong(0);
        
        // Start the integration manager
        start();
    }
    
    /**
     * Get the singleton instance of TTLIntegrationManager
     */
    public static TTLIntegrationManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Start the integration manager
     */
    private void start() {
        eventPublisher.publishEvent(TTLEventType.SYSTEM_STARTED, 
            "TTL Integration Manager Started", "System startup", null);
    }
    
    /**
     * Shutdown the integration manager
     */
    public void shutdown() {
        eventPublisher.shutdown();
        scheduler.shutdown();
        scheduledOverrides.clear();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ========================================
    // Event Publishing Integration
    // ========================================
    
    /**
     * Add an event listener for TTL management events
     */
    public void addEventListener(TTLEventListener listener) {
        eventPublisher.addListener(listener);
    }
    
    /**
     * Add an event listener using lambda expression
     */
    public void addLambdaEventListener(Consumer<TTLEvent> eventHandler) {
        eventPublisher.addListener(eventHandler);
    }
    
    /**
     * Remove an event listener
     */
    public void removeEventListener(TTLEventListener listener) {
        eventPublisher.removeListener(listener);
    }
    
    /**
     * Get current event listener count
     */
    public int getEventListenerCount() {
        return eventPublisher.getListenerCount();
    }
    
    // ========================================
    // TTL Management Operations
    // ========================================
    
    /**
     * Enable all TTL rules globally
     */
    public void enableAllTTL(String reason) {
        ttlManager.enableAllTTL();
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.GLOBAL_TTL_ENABLED, 
            "All TTL rules enabled", reason, null);
    }
    
    /**
     * Disable all TTL rules globally
     */
    public void disableAllTTL(String reason) {
        ttlManager.disableAllTTL();
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.GLOBAL_TTL_DISABLED, 
            "All TTL rules disabled", reason, null);
    }
    
    /**
     * Extend global TTL by specified days
     */
    public void extendGlobalTTL(int extraDays, String reason) {
        ttlManager.setGlobalTTLExtension(extraDays);
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.GLOBAL_TTL_EXTENDED, 
            "Global TTL extended by " + extraDays + " days", reason, extraDays);
    }
    
    /**
     * Override TTL for a specific class
     */
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        ttlManager.overrideClassTTL(clazz, override);
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.CLASS_TTL_OVERRIDDEN, 
            "Class TTL overridden for " + clazz.getSimpleName(), reason, 
            new TTLEventPublisher.ClassOverrideData(clazz, override));
    }
    
    /**
     * Override TTL for a specific method
     */
    public void overrideMethodTTL(Class<?> clazz, String methodName, TTLOverride override, String reason) {
        ttlManager.overrideMethodTTL(clazz, methodName, override);
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.METHOD_TTL_OVERRIDDEN, 
            "Method TTL overridden for " + clazz.getSimpleName() + "#" + methodName, reason,
            new TTLEventPublisher.MethodOverrideData(clazz, methodName, override));
    }
    
    /**
     * Override TTL for a specific field
     */
    public void overrideFieldTTL(Class<?> clazz, String fieldName, TTLOverride override, String reason) {
        ttlManager.overrideFieldTTL(clazz, fieldName, override);
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.FIELD_TTL_OVERRIDDEN, 
            "Field TTL overridden for " + clazz.getSimpleName() + "." + fieldName, reason,
            new TTLEventPublisher.FieldOverrideData(clazz, fieldName, override));
    }
    
    /**
     * Clear all TTL overrides
     */
    public void clearAllOverrides(String reason) {
        ttlManager.clearAllOverrides();
        scheduledOverrides.clear();
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.ALL_OVERRIDES_CLEARED, 
            "All TTL overrides cleared", reason, null);
    }
    
    // ========================================
    // Scheduled Override Management
    // ========================================
    
    /**
     * Schedule a TTL override that will be automatically removed after specified duration
     */
    public void scheduleOverride(String overrideId, Class<?> clazz, TTLOverride override, 
                               long durationMs, String reason) {
        // Apply the override immediately
        ttlManager.overrideClassTTL(clazz, override);
        
        // Schedule removal
        ScheduledOverride scheduledOverride = new ScheduledOverride(overrideId, clazz, override, reason);
        scheduledOverrides.put(overrideId, scheduledOverride);
        
        // Schedule automatic removal
        scheduler.schedule(() -> removeScheduledOverride(overrideId, reason), durationMs, TimeUnit.MILLISECONDS);
        
        operationCounter.incrementAndGet();
        eventPublisher.publishEvent(TTLEventType.TTL_OVERRIDE_SCHEDULED, 
            "TTL override scheduled for " + clazz.getSimpleName() + " (ID: " + overrideId + ")", reason,
            scheduledOverride);
    }
    
    /**
     * Remove a scheduled TTL override
     */
    public void removeScheduledOverride(String overrideId, String reason) {
        ScheduledOverride scheduledOverride = scheduledOverrides.remove(overrideId);
        if (scheduledOverride != null) {
            ttlManager.removeClassTTLOverride(scheduledOverride.getClazz());
            eventPublisher.publishEvent(TTLEventType.TTL_OVERRIDE_REMOVED, 
                "Scheduled TTL override removed for " + scheduledOverride.getClazz().getSimpleName() + 
                " (ID: " + overrideId + ")", reason, scheduledOverride);
        }
    }
    
    /**
     * Get all scheduled override IDs
     */
    public java.util.Set<String> getScheduledOverrideIds() {
        return scheduledOverrides.keySet();
    }
    
    /**
     * Get scheduled override count
     */
    public int getScheduledOverrideCount() {
        return scheduledOverrides.size();
    }
    
    // ========================================
    // Status and Monitoring
    // ========================================
    
    /**
     * Get comprehensive TTL management status
     */
    public TTLManagementStatus getStatus() {
        return new TTLManagementStatus(
            ttlManager.isGlobalTTLEnabled(),
            ttlManager.getGlobalTTLExtension(),
            ttlManager.getOverridesSummary(),
            getEventListenerCount(),
            eventPublisher.isEnabled()
        );
    }
    
    /**
     * Get total operation count
     */
    public long getTotalOperations() {
        return operationCounter.get();
    }
    
    /**
     * Get TTL manager instance for direct access
     */
    public TTLManager getTTLManager() {
        return ttlManager;
    }
    
    /**
     * Get event publisher instance for direct access
     */
    public TTLEventPublisher getEventPublisher() {
        return eventPublisher;
    }
    
    // ========================================
    // Utility Methods
    // ========================================
    
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
    
    // ========================================
    // Internal Classes
    // ========================================
    
    /**
     * Internal class for tracking scheduled overrides
     */
    private static class ScheduledOverride {
        private final String overrideId;
        private final Class<?> clazz;
        private final TTLOverride override;
        private final String reason;
        private final long scheduledTime;
        
        public ScheduledOverride(String overrideId, Class<?> clazz, TTLOverride override, String reason) {
            this.overrideId = overrideId;
            this.clazz = clazz;
            this.override = override;
            this.reason = reason;
            this.scheduledTime = System.currentTimeMillis();
        }
        
        public String getOverrideId() { return overrideId; }
        public Class<?> getClazz() { return clazz; }
        public TTLOverride getOverride() { return override; }
        public String getReason() { return reason; }
        public long getScheduledTime() { return scheduledTime; }
        
        @Override
        public String toString() {
            return "ScheduledOverride{id=" + overrideId + ", class=" + clazz.getSimpleName() + 
                   ", reason='" + reason + "', scheduled=" + scheduledTime + "}";
        }
    }
}

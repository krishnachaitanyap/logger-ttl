package com.logger.ttl.integration;

import com.logger.ttl.TTLManager;
import com.logger.ttl.TTLOverride;
import com.logger.ttl.TTLConfig;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Standalone event publisher for TTL management that works without Spring Boot.
 * Allows non-Spring applications to build custom integrations and update TTL configs during runtime.
 */
public class TTLEventPublisher {
    
    private static final TTLEventPublisher INSTANCE = new TTLEventPublisher();
    
    private final TTLManager ttlManager;
    private final CopyOnWriteArrayList<TTLEventListener> listeners;
    private final ExecutorService eventExecutor;
    private final AtomicBoolean enabled;
    
    private TTLEventPublisher() {
        this.ttlManager = TTLManager.getInstance();
        this.listeners = new CopyOnWriteArrayList<>();
        this.eventExecutor = Executors.newCachedThreadPool();
        this.enabled = new AtomicBoolean(true);
    }
    
    /**
     * Get the singleton instance of TTLEventPublisher
     */
    public static TTLEventPublisher getInstance() {
        return INSTANCE;
    }
    
    /**
     * Enable/disable event publishing
     */
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
    
    /**
     * Check if event publishing is enabled
     */
    public boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * Add an event listener
     */
    public void addListener(TTLEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove an event listener
     */
    public void removeListener(TTLEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Add a simple event listener using lambda
     */
    public void addListener(Consumer<TTLEvent> eventHandler) {
        addListener(new TTLEventListener() {
            @Override
            public void onTTLEvent(TTLEvent event) {
                eventHandler.accept(event);
            }
        });
    }
    
    /**
     * Remove all listeners
     */
    public void clearListeners() {
        listeners.clear();
    }
    
    /**
     * Get current listener count
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Publish a TTL event to all listeners
     */
    public void publishEvent(TTLEvent event) {
        if (!enabled.get() || listeners.isEmpty()) {
            return;
        }
        
        // Publish event asynchronously to avoid blocking
        eventExecutor.submit(() -> {
            for (TTLEventListener listener : listeners) {
                try {
                    listener.onTTLEvent(event);
                } catch (Exception e) {
                    // Log error but don't fail other listeners
                    System.err.println("Error in TTL event listener: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Publish event with automatic event creation
     */
    public void publishEvent(TTLEventType eventType, String message, String reason, Object data) {
        TTLEvent event = new TTLEvent(eventType, message, reason, data, System.currentTimeMillis());
        publishEvent(event);
    }
    
    /**
     * Enable all TTL rules globally and publish event
     */
    public void enableAllTTL(String reason) {
        ttlManager.enableAllTTL();
        publishEvent(TTLEventType.GLOBAL_TTL_ENABLED, "All TTL rules enabled", reason, null);
    }
    
    /**
     * Disable all TTL rules globally and publish event
     */
    public void disableAllTTL(String reason) {
        ttlManager.disableAllTTL();
        publishEvent(TTLEventType.GLOBAL_TTL_DISABLED, "All TTL rules disabled", reason, null);
    }
    
    /**
     * Extend global TTL and publish event
     */
    public void extendGlobalTTL(int extraDays, String reason) {
        ttlManager.setGlobalTTLExtension(extraDays);
        publishEvent(TTLEventType.GLOBAL_TTL_EXTENDED, 
                    "Global TTL extended by " + extraDays + " days", reason, extraDays);
    }
    
    /**
     * Override class TTL and publish event
     */
    public void overrideClassTTL(Class<?> clazz, TTLOverride override, String reason) {
        ttlManager.overrideClassTTL(clazz, override);
        publishEvent(TTLEventType.CLASS_TTL_OVERRIDDEN, 
                    "Class TTL overridden for " + clazz.getSimpleName(), reason, 
                    new ClassOverrideData(clazz, override));
    }
    
    /**
     * Override method TTL and publish event
     */
    public void overrideMethodTTL(Class<?> clazz, String methodName, TTLOverride override, String reason) {
        ttlManager.overrideMethodTTL(clazz, methodName, override);
        publishEvent(TTLEventType.METHOD_TTL_OVERRIDDEN, 
                    "Method TTL overridden for " + clazz.getSimpleName() + "#" + methodName, reason,
                    new MethodOverrideData(clazz, methodName, override));
    }
    
    /**
     * Override field TTL and publish event
     */
    public void overrideFieldTTL(Class<?> clazz, String fieldName, TTLOverride override, String reason) {
        ttlManager.overrideFieldTTL(clazz, fieldName, override);
        publishEvent(TTLEventType.FIELD_TTL_OVERRIDDEN, 
                    "Field TTL overridden for " + clazz.getSimpleName() + "." + fieldName, reason,
                    new FieldOverrideData(clazz, fieldName, override));
    }
    
    /**
     * Clear all overrides and publish event
     */
    public void clearAllOverrides(String reason) {
        ttlManager.clearAllOverrides();
        publishEvent(TTLEventType.ALL_OVERRIDES_CLEARED, "All TTL overrides cleared", reason, null);
    }
    
    /**
     * Get current TTL management status
     */
    public TTLManagementStatus getStatus() {
        return new TTLManagementStatus(
            ttlManager.isGlobalTTLEnabled(),
            ttlManager.getGlobalTTLExtension(),
            ttlManager.getOverridesSummary(),
            getListenerCount(),
            enabled.get()
        );
    }
    
    /**
     * Shutdown the event publisher
     */
    public void shutdown() {
        enabled.set(false);
        eventExecutor.shutdown();
        listeners.clear();
    }
    
    /**
     * Check if publisher is shutdown
     */
    public boolean isShutdown() {
        return eventExecutor.isShutdown();
    }
    
    /**
     * Data classes for event payloads
     */
    public static class ClassOverrideData {
        private final Class<?> clazz;
        private final TTLOverride override;
        
        public ClassOverrideData(Class<?> clazz, TTLOverride override) {
            this.clazz = clazz;
            this.override = override;
        }
        
        public Class<?> getClazz() { return clazz; }
        public TTLOverride getOverride() { return override; }
        
        @Override
        public String toString() {
            return "ClassOverrideData{clazz=" + clazz.getSimpleName() + ", override=" + override + "}";
        }
    }
    
    public static class MethodOverrideData {
        private final Class<?> clazz;
        private final String methodName;
        private final TTLOverride override;
        
        public MethodOverrideData(Class<?> clazz, String methodName, TTLOverride override) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.override = override;
        }
        
        public Class<?> getClazz() { return clazz; }
        public String getMethodName() { return methodName; }
        public TTLOverride getOverride() { return override; }
        
        @Override
        public String toString() {
            return "MethodOverrideData{clazz=" + clazz.getSimpleName() + 
                   ", method=" + methodName + ", override=" + override + "}";
        }
    }
    
    public static class FieldOverrideData {
        private final Class<?> clazz;
        private final String fieldName;
        private final TTLOverride override;
        
        public FieldOverrideData(Class<?> clazz, String fieldName, TTLOverride override) {
            this.clazz = clazz;
            this.fieldName = fieldName;
            this.override = override;
        }
        
        public Class<?> getClazz() { return clazz; }
        public String getFieldName() { return fieldName; }
        public TTLOverride getOverride() { return override; }
        
        @Override
        public String toString() {
            return "FieldOverrideData{clazz=" + clazz.getSimpleName() + 
                   ", field=" + fieldName + ", override=" + override + "}";
        }
    }
}

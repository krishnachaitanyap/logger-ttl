package com.logger.ttl;

import com.logger.ttl.metrics.TTLMetricsManager;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages TTL rules dynamically at runtime, allowing expired logs to be re-enabled
 * and TTL configurations to be modified without application restart.
 */
public class TTLManager {
    
    private static final TTLManager INSTANCE = new TTLManager();
    
    // Global TTL override - when true, all TTL rules are bypassed
    private final AtomicBoolean globalTTLOverride = new AtomicBoolean(false);
    
    // Global TTL extension - adds extra days to all TTL calculations
    private final AtomicReference<Integer> globalTTLExtension = new AtomicReference<>(0);
    
    // Per-class TTL overrides
    private final ConcurrentMap<Class<?>, TTLOverride> classOverrides = new ConcurrentHashMap<>();
    
    // Per-method TTL overrides
    private final ConcurrentMap<String, TTLOverride> methodOverrides = new ConcurrentHashMap<>();
    
    // Per-field TTL overrides
    private final ConcurrentMap<String, TTLOverride> fieldOverrides = new ConcurrentHashMap<>();
    
    private TTLManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of TTLManager
     */
    public static TTLManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Enable all TTL rules globally (bypass all expiration)
     */
    public void enableAllTTL() {
        globalTTLOverride.set(true);
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("global_enable");
    }
    
    /**
     * Disable all TTL rules globally (enforce all expiration)
     */
    public void disableAllTTL() {
        globalTTLOverride.set(false);
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("global_disable");
    }
    
    /**
     * Check if global TTL override is enabled
     */
    public boolean isGlobalTTLEnabled() {
        return globalTTLOverride.get();
    }
    
    /**
     * Set global TTL extension (adds extra days to all TTL calculations)
     */
    public void setGlobalTTLExtension(int extraDays) {
        globalTTLExtension.set(extraDays);
    }
    
    /**
     * Get current global TTL extension
     */
    public int getGlobalTTLExtension() {
        return globalTTLExtension.get();
    }
    
    /**
     * Override TTL for a specific class
     */
    public void overrideClassTTL(Class<?> clazz, TTLOverride override) {
        classOverrides.put(clazz, override);
        TTLMetricsManager.getInstance().getMetrics().recordOverrideOperation(override.getType());
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("class_override");
    }
    
    /**
     * Remove TTL override for a specific class
     */
    public void removeClassTTLOverride(Class<?> clazz) {
        classOverrides.remove(clazz);
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("class_override_remove");
    }
    
    /**
     * Override TTL for a specific method
     */
    public void overrideMethodTTL(Class<?> clazz, String methodName, TTLOverride override) {
        String key = clazz.getName() + "#" + methodName;
        methodOverrides.put(key, override);
        TTLMetricsManager.getInstance().getMetrics().recordOverrideOperation(override.getType());
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("method_override");
    }
    
    /**
     * Remove TTL override for a specific method
     */
    public void removeMethodTTLOverride(Class<?> clazz, String methodName) {
        String key = clazz.getName() + "#" + methodName;
        methodOverrides.remove(key);
    }
    
    /**
     * Override TTL for a specific field
     */
    public void overrideFieldTTL(Class<?> clazz, String fieldName, TTLOverride override) {
        String key = clazz.getName() + "." + fieldName;
        fieldOverrides.put(key, override);
    }
    
    /**
     * Remove TTL override for a specific field
     */
    public void removeFieldTTLOverride(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        fieldOverrides.remove(key);
    }
    
    /**
     * Clear all TTL overrides
     */
    public void clearAllOverrides() {
        classOverrides.clear();
        methodOverrides.clear();
        fieldOverrides.clear();
        globalTTLOverride.set(false);
        globalTTLExtension.set(0);
        TTLMetricsManager.getInstance().getMetrics().recordConfigurationChange("clear_all");
    }
    
    /**
     * Get TTL override for a class
     */
    public TTLOverride getClassTTLOverride(Class<?> clazz) {
        return classOverrides.get(clazz);
    }
    
    /**
     * Get TTL override for a method
     */
    public TTLOverride getMethodTTLOverride(Class<?> clazz, String methodName) {
        String key = clazz.getName() + "#" + methodName;
        return methodOverrides.get(key);
    }
    
    /**
     * Get TTL override for a field
     */
    public TTLOverride getFieldTTLOverride(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return fieldOverrides.get(key);
    }
    
    /**
     * Check if a TTL configuration should be bypassed based on runtime overrides
     */
    public boolean shouldBypassTTL(Class<?> clazz, String methodName, String fieldName, TTLConfig originalConfig) {
        // Check global override first
        if (globalTTLOverride.get()) {
            return true;
        }
        
        // Check class-level override
        TTLOverride classOverride = getClassTTLOverride(clazz);
        if (classOverride != null && classOverride.shouldBypass(originalConfig)) {
            return true;
        }
        
        // Check method-level override
        if (methodName != null) {
            TTLOverride methodOverride = getMethodTTLOverride(clazz, methodName);
            if (methodOverride != null && methodOverride.shouldBypass(originalConfig)) {
                return true;
            }
        }
        
        // Check field-level override
        if (fieldName != null) {
            TTLOverride fieldOverride = getFieldTTLOverride(clazz, fieldName);
            if (fieldOverride != null && fieldOverride.shouldBypass(originalConfig)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Apply global TTL extension to a TTL configuration
     */
    public TTLConfig applyGlobalExtension(TTLConfig originalConfig) {
        int extension = globalTTLExtension.get();
        if (extension <= 0) {
            return originalConfig;
        }
        
        // Create a new TTLConfig with extended TTL
        String startDate = originalConfig.getStartTime() != null ? 
            originalConfig.getStartTime().toString() : "";
        return new TTLConfig(
            startDate,
            originalConfig.getTtlDays() + extension,
            originalConfig.getLevels().toArray(new LogLevel[0])
        );
    }
    
    /**
     * Get summary of current TTL overrides
     */
    public String getOverridesSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TTL Manager Status:\n");
        summary.append("Global TTL Override: ").append(globalTTLOverride.get()).append("\n");
        summary.append("Global TTL Extension: +").append(globalTTLExtension.get()).append(" days\n");
        summary.append("Class Overrides: ").append(classOverrides.size()).append("\n");
        summary.append("Method Overrides: ").append(methodOverrides.size()).append("\n");
        summary.append("Field Overrides: ").append(fieldOverrides.size()).append("\n");
        return summary.toString();
    }
    
    /**
     * Get count of active TTL configurations (for metrics)
     */
    public int getActiveConfigurationsCount() {
        return classOverrides.size() + methodOverrides.size() + fieldOverrides.size();
    }
    
    /**
     * Get count of class overrides (for metrics)
     */
    public int getClassOverridesCount() {
        return classOverrides.size();
    }
    
    /**
     * Get count of method overrides (for metrics)
     */
    public int getMethodOverridesCount() {
        return methodOverrides.size();
    }
    
    /**
     * Get count of field overrides (for metrics)
     */
    public int getFieldOverridesCount() {
        return fieldOverrides.size();
    }
    
    /**
     * Update metrics with current configuration counts
     */
    public void updateMetrics() {
        TTLMetricsManager metricsManager = TTLMetricsManager.getInstance();
        metricsManager.getMetrics().updateActiveConfigsCount(getActiveConfigurationsCount());
        metricsManager.getMetrics().updateOverrideConfigsCount(getActiveConfigurationsCount());
    }
}

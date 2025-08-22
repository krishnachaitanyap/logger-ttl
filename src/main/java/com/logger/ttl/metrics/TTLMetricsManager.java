package com.logger.ttl.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Manager for TTL metrics that provides a singleton instance
 * and handles meter registry creation and configuration.
 */
public class TTLMetricsManager {
    
    private static volatile TTLMetricsManager instance;
    private static final Object lock = new Object();
    
    private TTLMetrics metrics;
    private MeterRegistry meterRegistry;
    
    private TTLMetricsManager() {
        // Create a simple meter registry by default
        // In production, this would typically be injected or configured
        this.meterRegistry = createDefaultMeterRegistry();
        this.metrics = new TTLMetrics(meterRegistry);
    }
    
    /**
     * Get the singleton instance of TTLMetricsManager
     */
    public static TTLMetricsManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new TTLMetricsManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get the TTL metrics instance
     */
    public TTLMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Get the underlying meter registry
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
    
    /**
     * Set a custom meter registry (useful for testing or custom configuration)
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            // Create new metrics with the new registry
            TTLMetrics newMetrics = new TTLMetrics(meterRegistry);
            
            // Update the instance
            synchronized (lock) {
                this.metrics = newMetrics;
                this.meterRegistry = meterRegistry;
            }
        }
    }
    
    /**
     * Create a default meter registry
     */
    private MeterRegistry createDefaultMeterRegistry() {
        try {
            // Try to create a Prometheus registry first
            return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        } catch (Exception e) {
            // Fall back to simple registry if Prometheus is not available
            return new SimpleMeterRegistry();
        }
    }
    
    /**
     * Get metrics summary for debugging
     */
    public String getMetricsSummary() {
        return metrics.getMetricsSummary();
    }
    
    /**
     * Reset all metrics (useful for testing)
     */
    public void resetMetrics() {
        // Create a new metrics instance with the same registry
        this.metrics = new TTLMetrics(meterRegistry);
    }
}

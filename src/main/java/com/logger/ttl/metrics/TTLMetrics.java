package com.logger.ttl.metrics;

import com.logger.ttl.LogLevel;
import com.logger.ttl.TTLOverride;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics collection for the TTL Framework.
 * Provides counters, gauges, and timers for monitoring TTL operations.
 */
public class TTLMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Counters for different TTL operations
    private final Counter expiredLogsCounter;
    private final Counter activeLogsCounter;
    private final Counter bypassedLogsCounter;
    private final Counter overrideOperationsCounter;
    private final Counter configurationChangesCounter;
    
    // Gauges for current state
    private final AtomicInteger activeTTLConfigsCount;
    private final AtomicInteger expiredTTLConfigsCount;
    private final AtomicInteger overrideConfigsCount;
    
    // Timers for performance
    private final Timer ttlValidationTimer;
    private final Timer annotationProcessingTimer;
    
    // Custom metrics
    private final Gauge activeTTLConfigsGauge;
    private final Gauge expiredTTLConfigsGauge;
    private final Gauge overrideConfigsGauge;
    
    public TTLMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeTTLConfigsCount = new AtomicInteger(0);
        this.expiredTTLConfigsCount = new AtomicInteger(0);
        this.overrideConfigsCount = new AtomicInteger(0);
        
        // Initialize counters
        this.expiredLogsCounter = Counter.builder("ttl.logs.expired")
            .description("Total number of expired logs")
            .register(meterRegistry);
            
        this.activeLogsCounter = Counter.builder("ttl.logs.active")
            .description("Total number of active logs")
            .register(meterRegistry);
            
        this.bypassedLogsCounter = Counter.builder("ttl.logs.bypassed")
            .description("Total number of bypassed logs")
            .register(meterRegistry);
            
        this.overrideOperationsCounter = Counter.builder("ttl.override.operations")
            .description("Total number of TTL override operations")
            .register(meterRegistry);
            
        this.configurationChangesCounter = Counter.builder("ttl.configuration.changes")
            .description("Total number of TTL configuration changes")
            .register(meterRegistry);
            
        // Initialize timers
        this.ttlValidationTimer = Timer.builder("ttl.validation.duration")
            .description("Time taken for TTL validation")
            .register(meterRegistry);
            
        this.annotationProcessingTimer = Timer.builder("ttl.annotation.processing.duration")
            .description("Time taken for annotation processing")
            .register(meterRegistry);
            
        // Initialize gauges
        this.activeTTLConfigsGauge = Gauge.builder("ttl.configs.active.count", activeTTLConfigsCount, AtomicInteger::get)
            .description("Current number of active TTL configurations")
            .register(meterRegistry);
            
        this.expiredTTLConfigsGauge = Gauge.builder("ttl.configs.expired.count", expiredTTLConfigsCount, AtomicInteger::get)
            .description("Current number of expired TTL configurations")
            .register(meterRegistry);
            
        this.overrideConfigsGauge = Gauge.builder("ttl.configs.override.count", overrideConfigsCount, AtomicInteger::get)
            .description("Current number of override configurations")
            .register(meterRegistry);
    }
    
    /**
     * Record an expired log with level information
     */
    public void recordExpiredLog(LogLevel level) {
        expiredLogsCounter.increment();
    }
    
    /**
     * Record an active log with level information
     */
    public void recordActiveLog(LogLevel level) {
        activeLogsCounter.increment();
    }
    
    /**
     * Record a bypassed log with level information
     */
    public void recordBypassedLog(LogLevel level) {
        bypassedLogsCounter.increment();
    }
    
    /**
     * Record an override operation with type information
     */
    public void recordOverrideOperation(TTLOverride.OverrideType type) {
        overrideOperationsCounter.increment();
    }
    
    /**
     * Record a configuration change operation
     */
    public void recordConfigurationChange(String operation) {
        configurationChangesCounter.increment();
    }
    
    /**
     * Start a TTL validation timer
     */
    public Timer.Sample startValidationTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Start an annotation processing timer
     */
    public Timer.Sample startAnnotationProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Update the count of active TTL configurations
     */
    public void updateActiveConfigsCount(int count) {
        activeTTLConfigsCount.set(count);
    }
    
    /**
     * Update the count of expired TTL configurations
     */
    public void updateExpiredConfigsCount(int count) {
        expiredTTLConfigsCount.set(count);
    }
    
    /**
     * Update the count of override configurations
     */
    public void updateOverrideConfigsCount(int count) {
        overrideConfigsCount.set(count);
    }
    
    /**
     * Get the underlying meter registry
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
    
    /**
     * Get count of active logs
     */
    public long getActiveLogsCount() {
        return activeLogsCounter.count();
    }
    
    /**
     * Get count of expired logs
     */
    public long getExpiredLogsCount() {
        return expiredLogsCounter.count();
    }
    
    /**
     * Get count of bypassed logs
     */
    public long getBypassedLogsCount() {
        return bypassedLogsCounter.count();
    }
    
    /**
     * Get count of override operations
     */
    public long getOverrideOperationsCount() {
        return overrideOperationsCounter.count();
    }
    
    /**
     * Get count of configuration changes
     */
    public long getConfigurationChangesCount() {
        return configurationChangesCounter.count();
    }
    
    /**
     * Get count of active configurations
     */
    public int getActiveConfigsCount() {
        return activeTTLConfigsCount.get();
    }
    
    /**
     * Get count of expired configurations
     */
    public int getExpiredConfigsCount() {
        return expiredTTLConfigsCount.get();
    }
    
    /**
     * Get count of override configurations
     */
    public int getOverrideConfigsCount() {
        return overrideConfigsCount.get();
    }
    
    /**
     * Get all TTL-related metrics as a formatted string for debugging
     */
    public String getMetricsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TTL Metrics Summary:\n");
        summary.append("  Active Logs: ").append(getActiveLogsCount()).append("\n");
        summary.append("  Expired Logs: ").append(getExpiredLogsCount()).append("\n");
        summary.append("  Bypassed Logs: ").append(getBypassedLogsCount()).append("\n");
        summary.append("  Override Operations: ").append(getOverrideOperationsCount()).append("\n");
        summary.append("  Configuration Changes: ").append(getConfigurationChangesCount()).append("\n");
        summary.append("  Active Configs: ").append(getActiveConfigsCount()).append("\n");
        summary.append("  Expired Configs: ").append(getExpiredConfigsCount()).append("\n");
        summary.append("  Override Configs: ").append(getOverrideConfigsCount()).append("\n");
        return summary.toString();
    }
}

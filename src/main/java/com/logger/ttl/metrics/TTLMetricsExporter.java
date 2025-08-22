package com.logger.ttl.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Exports TTL metrics in various formats for integration with existing application infrastructure.
 * This class does not run its own HTTP server but provides metrics data that can be exposed
 * through the application's existing endpoints (e.g., Spring Boot Actuator).
 */
public class TTLMetricsExporter {
    
    private final TTLMetricsManager metricsManager;
    
    public TTLMetricsExporter() {
        this.metricsManager = TTLMetricsManager.getInstance();
    }
    
    /**
     * Get metrics in Prometheus format for Spring Boot Actuator integration
     */
    public String getPrometheusMetrics() {
        try {
            MeterRegistry registry = metricsManager.getMeterRegistry();
            if (registry instanceof PrometheusMeterRegistry) {
                return ((PrometheusMeterRegistry) registry).scrape();
            } else {
                // Fallback for non-Prometheus registries
                return "# TTL Framework Metrics (Fallback Format)\n" +
                       "# Note: Using fallback metrics format\n" +
                       metricsManager.getMetricsSummary();
            }
        } catch (Exception e) {
            return "# Error retrieving metrics: " + e.getMessage() + "\n";
        }
    }
    
    /**
     * Get metrics summary as formatted text
     */
    public String getMetricsSummary() {
        return metricsManager.getMetricsSummary();
    }
    
    /**
     * Get metrics in JSON format for REST API integration
     */
    public String getMetricsJson() {
        TTLMetrics metrics = metricsManager.getMetrics();
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"ttl_framework\": {\n");
        json.append("    \"metrics\": {\n");
        json.append("      \"active_logs\": ").append(metrics.getActiveLogsCount()).append(",\n");
        json.append("      \"expired_logs\": ").append(metrics.getExpiredLogsCount()).append(",\n");
        json.append("      \"bypassed_logs\": ").append(metrics.getBypassedLogsCount()).append(",\n");
        json.append("      \"override_operations\": ").append(metrics.getOverrideOperationsCount()).append(",\n");
        json.append("      \"configuration_changes\": ").append(metrics.getConfigurationChangesCount()).append("\n");
        json.append("    },\n");
        json.append("    \"configurations\": {\n");
        json.append("      \"active_configs\": ").append(metrics.getActiveConfigsCount()).append(",\n");
        json.append("      \"expired_configs\": ").append(metrics.getExpiredConfigsCount()).append(",\n");
        json.append("      \"override_configs\": ").append(metrics.getOverrideConfigsCount()).append("\n");
        json.append("    }\n");
        json.append("  }\n");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Get metrics in a simple key-value format
     */
    public String getMetricsKeyValue() {
        TTLMetrics metrics = metricsManager.getMetrics();
        StringBuilder kv = new StringBuilder();
        kv.append("ttl_active_logs=").append(metrics.getActiveLogsCount()).append("\n");
        kv.append("ttl_expired_logs=").append(metrics.getExpiredLogsCount()).append("\n");
        kv.append("ttl_bypassed_logs=").append(metrics.getBypassedLogsCount()).append("\n");
        kv.append("ttl_override_operations=").append(metrics.getOverrideOperationsCount()).append("\n");
        kv.append("ttl_configuration_changes=").append(metrics.getConfigurationChangesCount()).append("\n");
        kv.append("ttl_active_configs=").append(metrics.getActiveConfigsCount()).append("\n");
        kv.append("ttl_expired_configs=").append(metrics.getExpiredConfigsCount()).append("\n");
        kv.append("ttl_override_configs=").append(metrics.getOverrideConfigsCount());
        return kv.toString();
    }
    
    /**
     * Check if metrics are available
     */
    public boolean isMetricsAvailable() {
        try {
            return metricsManager.getMeterRegistry() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the underlying meter registry for direct access
     */
    public MeterRegistry getMeterRegistry() {
        return metricsManager.getMeterRegistry();
    }
    
    /**
     * Reset all metrics (useful for testing)
     */
    public void resetMetrics() {
        metricsManager.resetMetrics();
    }
}

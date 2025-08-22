package com.logger.ttl.examples;

import com.logger.ttl.TTLLogger;
import com.logger.ttl.TTLLoggerFactory;
import com.logger.ttl.LogTTL;
import com.logger.ttl.LogLevel;
import com.logger.ttl.metrics.TTLMetricsExporter;
import com.logger.ttl.metrics.TTLMetricsManager;

/**
 * Example demonstrating TTL metrics integration.
 * This shows how to collect and export TTL metrics in various formats
 * for integration with monitoring systems like Prometheus and Grafana.
 */
public class MetricsIntegrationExample {
    
    private static final TTLLogger logger = TTLLoggerFactory.getLogger(MetricsIntegrationExample.class);
    
    @LogTTL(ttlDays = 7, levels = {LogLevel.DEBUG, LogLevel.INFO})
    public void demonstrateMetricsCollection() {
        logger.info("This INFO log will be recorded in metrics");
        logger.debug("This DEBUG log will be recorded in metrics");
        logger.warn("This WARN log is not affected by TTL");
        logger.error("This ERROR log is not affected by TTL");
        
        // Simulate some expired logs
        logger.debug("This DEBUG log might expire based on TTL");
    }
    
    @LogTTL(start = "2024-01-01T00:00:00Z", ttlDays = 30)
    public void demonstrateExpiredLogs() {
        // These logs will be marked as expired in metrics
        logger.info("This log has an expired start date");
        logger.debug("This debug log also has an expired start date");
    }
    
    public void demonstrateMetricsExport() {
        System.out.println("=== TTL Metrics Integration Example ===\n");
        
        // 1. Generate some log activity
        demonstrateMetricsCollection();
        demonstrateExpiredLogs();
        
        // 2. Export metrics in different formats
        TTLMetricsExporter exporter = new TTLMetricsExporter();
        
        System.out.println("1. Metrics Summary (Text Format):");
        System.out.println(exporter.getMetricsSummary());
        
        System.out.println("\n2. Metrics in JSON Format:");
        System.out.println(exporter.getMetricsJson());
        
        System.out.println("\n3. Metrics in Key-Value Format:");
        System.out.println(exporter.getMetricsKeyValue());
        
        System.out.println("\n4. Prometheus Format:");
        System.out.println(exporter.getPrometheusMetrics());
        
        // 3. Demonstrate metrics manager
        System.out.println("\n5. Metrics Manager Status:");
        TTLMetricsManager manager = TTLMetricsManager.getInstance();
        System.out.println("Metrics Available: " + exporter.isMetricsAvailable());
        System.out.println("Meter Registry Type: " + manager.getMeterRegistry().getClass().getSimpleName());
        
        // 4. Show how to integrate with existing monitoring
        System.out.println("\n=== Integration Instructions ===");
        System.out.println("For Spring Boot applications:");
        System.out.println("- Add spring-boot-starter-actuator dependency");
        System.out.println("- Configure management.endpoints.web.exposure.include=prometheus,ttl-metrics");
        System.out.println("- Access metrics at /actuator/prometheus and /actuator/ttl-metrics");
        
        System.out.println("\nFor non-Spring applications:");
        System.out.println("- Use TTLMetricsExporter to get metrics in desired format");
        System.out.println("- Expose metrics through your existing HTTP endpoints");
        System.out.println("- Configure Prometheus to scrape your metrics endpoint");
        
        System.out.println("\nFor Grafana dashboards:");
        System.out.println("- Use the Prometheus data source");
        System.out.println("- Create queries like: ttl_logs_active_total, ttl_logs_expired_total");
        System.out.println("- Set up alerts for unusual TTL behavior");
    }
    
    public static void main(String[] args) {
        MetricsIntegrationExample example = new MetricsIntegrationExample();
        example.demonstrateMetricsExport();
    }
}

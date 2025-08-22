# Prometheus & Grafana Integration for TTL Framework

This document explains how to integrate the Logger TTL Framework with Prometheus and Grafana for comprehensive monitoring and observability.

## 🎯 **Why No Separate HTTP Server?**

The TTL Framework is designed to be **integrated into existing applications**, not run as a standalone service. This approach provides several benefits:

### **Advantages of Integration**
- **No Port Conflicts**: Uses existing application HTTP infrastructure
- **Centralized Management**: Single HTTP server, single configuration
- **Security**: Inherits application's security and authentication
- **Resource Efficiency**: No additional processes or memory overhead
- **Deployment Simplicity**: No need to manage separate services

### **Integration Patterns**
1. **Spring Boot**: Use `/actuator/prometheus` endpoint
2. **Custom Applications**: Expose metrics through existing HTTP endpoints
3. **Microservices**: Each service exposes its own TTL metrics
4. **Kubernetes**: Use service discovery for dynamic scraping

## 🏗️ **Architecture Overview**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │   Prometheus    │    │     Grafana     │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ TTL Logger  │ │    │ │   Scraper   │ │    │ │  Dashboard  │ │
│ │             │ │    │ │             │ │    │ │             │ │
│ │ ┌─────────┐ │ │    │ │ ┌─────────┐ │ │    │ │ ┌─────────┐ │ │
│ │ │Metrics  │ │ │    │ │ │Metrics  │ │ │    │ │ │Charts   │ │ │
│ │ │Collector│ │ │    │ │ │Storage  │ │ │    │ │ │& Graphs│ │ │
│ │ └─────────┘ │ │    │ │ └─────────┘ │ │    │ │ └─────────┘ │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │                 │    │                 │
│ │HTTP Endpoint│ │◄───┤                 │    │                 │
│ │/metrics     │ │    │                 │    │                 │
│ └─────────────┘ │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔧 **Implementation Components**

### **1. TTLMetrics Class**
Collects and exposes TTL framework metrics:
- **Counters**: Active logs, expired logs, bypassed logs
- **Gauges**: Configuration counts, override counts
- **Timers**: TTL validation performance, annotation processing

### **2. TTLMetricsManager**
Singleton manager for metrics configuration:
- **Meter Registry Management**: Handles Prometheus vs Simple registry
- **Metrics Lifecycle**: Creation, updates, and cleanup
- **Configuration**: Allows custom meter registry injection

### **3. TTLMetricsExporter**
Exports metrics in various formats:
- **Prometheus Format**: For direct scraping
- **JSON Format**: For REST API integration
- **Key-Value Format**: For simple monitoring systems
- **Text Format**: For human-readable output

## 📊 **Available Metrics**

### **Log Status Metrics**
```
# HELP ttl_logs_active_total Total number of active logs
# TYPE ttl_logs_active_total counter
ttl_logs_active_total 1234

# HELP ttl_logs_expired_total Total number of expired logs
# TYPE ttl_logs_expired_total counter
ttl_logs_expired_total 567

# HELP ttl_logs_bypassed_total Total number of bypassed logs
# TYPE ttl_logs_bypassed_total counter
ttl_logs_bypassed_total 89
```

### **Configuration Metrics**
```
# HELP ttl_configs_active_count Current number of active TTL configurations
# TYPE ttl_configs_active_count gauge
ttl_configs_active_count 15

# HELP ttl_configs_expired_count Current number of expired TTL configurations
# TYPE ttl_configs_expired_count gauge
ttl_configs_expired_count 3

# HELP ttl_configs_override_count Current number of override configurations
# TYPE ttl_configs_override_count gauge
ttl_configs_override_count 7
```

### **Operation Metrics**
```
# HELP ttl_override_operations_total Total number of TTL override operations
# TYPE ttl_override_operations_total counter
ttl_override_operations_total 25

# HELP ttl_configuration_changes_total Total number of TTL configuration changes
# TYPE ttl_configuration_changes_total counter
ttl_configuration_changes_total 12
```

### **Performance Metrics**
```
# HELP ttl_validation_duration_seconds Time taken for TTL validation
# TYPE ttl_validation_duration_seconds histogram
ttl_validation_duration_seconds_bucket{le="0.001"} 1000
ttl_validation_duration_seconds_bucket{le="0.01"} 1500
ttl_validation_duration_seconds_bucket{le="0.1"} 2000
ttl_validation_duration_seconds_bucket{le="+Inf"} 2000

# HELP ttl_annotation_processing_duration_seconds Time taken for annotation processing
# TYPE ttl_annotation_processing_duration_seconds histogram
```

## 🚀 **Integration Examples**

### **Spring Boot Integration**

#### **1. Add Dependencies**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

#### **2. Configuration**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### **3. Access Metrics**
- **Prometheus Format**: `GET /actuator/prometheus`
- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`

### **Custom Application Integration**

#### **1. Create Metrics Endpoint**
```java
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final TTLMetricsExporter metricsExporter;
    
    @GetMapping("/ttl")
    public String getTTLMetrics() {
        return metricsExporter.getPrometheusMetrics();
    }
    
    @GetMapping("/ttl/json")
    public String getTTLMetricsJson() {
        return metricsExporter.getMetricsJson();
    }
}
```

#### **2. Configure Prometheus Scraping**
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'ttl-custom-app'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/api/metrics/ttl'
    scrape_interval: 15s
```

### **Non-Spring Application Integration**

#### **1. Use TTLMetricsExporter**
```java
public class MetricsEndpoint {
    
    private final TTLMetricsExporter exporter = new TTLMetricsExporter();
    
    public void handleMetricsRequest(HttpExchange exchange) throws IOException {
        String metrics = exporter.getPrometheusMetrics();
        
        exchange.getResponseHeaders().add("Content-Type", 
            "text/plain; version=0.0.4; charset=utf-8");
        exchange.sendResponseHeaders(200, metrics.length());
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(metrics.getBytes(StandardCharsets.UTF_8));
        }
    }
}
```

## 📈 **Grafana Dashboard Setup**

### **1. Import Dashboard**
1. Open Grafana
2. Click "+" → "Import"
3. Upload `grafana-dashboard.json`
4. Select Prometheus data source
5. Import dashboard

### **2. Key Queries**

#### **Log Status Overview**
```promql
# Active logs rate
rate(ttl_logs_active_total[5m])

# Expired logs rate
rate(ttl_logs_expired_total[5m])

# Bypassed logs rate
rate(ttl_logs_bypassed_total[5m])
```

#### **Performance Metrics**
```promql
# TTL validation latency (95th percentile)
histogram_quantile(0.95, rate(ttl_validation_duration_seconds_bucket[5m]))

# Configuration change rate
rate(ttl_configuration_changes_total[5m])
```

#### **Alerting Rules**
```yaml
# ttl-alerts.yml
groups:
  - name: ttl-framework
    rules:
      - alert: HighTTLExpirationRate
        expr: rate(ttl_logs_expired_total[5m]) > 10
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High TTL log expiration rate"
          description: "TTL logs are expiring at {{ $value }} logs/sec"
      
      - alert: TTLValidationLatencyHigh
        expr: histogram_quantile(0.95, rate(ttl_validation_duration_seconds_bucket[5m])) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High TTL validation latency"
          description: "95th percentile TTL validation latency is {{ $value }}s"
```

## 🔍 **Monitoring Best Practices**

### **1. Key Metrics to Watch**
- **Log Expiration Rate**: Sudden spikes may indicate TTL misconfiguration
- **Validation Latency**: Performance degradation in TTL processing
- **Override Operations**: Unusual runtime TTL changes
- **Configuration Counts**: Memory usage and complexity

### **2. Alerting Strategy**
- **Warning**: Expiration rate > 10 logs/sec for 2 minutes
- **Critical**: Validation latency > 100ms for 5 minutes
- **Info**: Configuration changes for audit purposes

### **3. Dashboard Organization**
- **Overview**: High-level TTL status and health
- **Performance**: Latency and throughput metrics
- **Operations**: Runtime TTL management activities
- **Business**: Log retention and compliance metrics

## 🧪 **Testing Metrics Integration**

### **1. Run Example Application**
```bash
mvn exec:java -Dexec.mainClass="com.logger.ttl.examples.MetricsIntegrationExample"
```

### **2. Verify Metrics Endpoint**
```bash
# Test Prometheus format
curl http://localhost:8080/actuator/prometheus | grep ttl

# Test JSON format
curl http://localhost:8080/actuator/ttl-metrics
```

### **3. Validate Prometheus Scraping**
1. Start Prometheus with provided configuration
2. Check targets page for successful scraping
3. Verify metrics appear in Prometheus UI
4. Test queries in Prometheus

## 🚨 **Troubleshooting**

### **Common Issues**

#### **1. Metrics Not Appearing**
- Check if Micrometer dependencies are included
- Verify meter registry configuration
- Ensure TTL framework is being used

#### **2. Prometheus Scraping Fails**
- Verify endpoint accessibility
- Check firewall and network configuration
- Validate Prometheus configuration syntax

#### **3. High Memory Usage**
- Monitor configuration counts
- Check for memory leaks in TTL configurations
- Review TTL policy settings

### **Debug Commands**
```bash
# Check metrics availability
curl -s http://localhost:8080/actuator/health | jq '.components.ttl'

# Verify Prometheus format
curl -s http://localhost:8080/actuator/prometheus | head -20

# Test specific metric
curl -s http://localhost:8080/actuator/prometheus | grep "ttl_logs_active_total"
```

## 📚 **Additional Resources**

- **Micrometer Documentation**: https://micrometer.io/docs
- **Prometheus Configuration**: https://prometheus.io/docs/prometheus/latest/configuration/
- **Grafana Dashboards**: https://grafana.com/docs/grafana/latest/dashboards/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

## 🎉 **Summary**

The TTL Framework provides comprehensive metrics collection without requiring a separate HTTP server. By integrating with existing application infrastructure, it offers:

- **Seamless Integration**: Works with Spring Boot, custom apps, and microservices
- **Rich Metrics**: Detailed insights into TTL operations and performance
- **Multiple Formats**: Prometheus, JSON, and text output options
- **Production Ready**: Includes alerting, monitoring, and troubleshooting support

This approach ensures that TTL metrics become part of your application's overall observability strategy, providing valuable insights into logging behavior and TTL policy effectiveness.

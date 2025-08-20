package com.logger.ttl.spring;

import com.logger.ttl.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing TTL rules in Spring Boot applications.
 * Provides HTTP endpoints for runtime TTL management.
 * 
 * Note: This controller requires Spring Boot dependencies to be available.
 * In a real Spring Boot application, you would add @RestController annotation.
 */
public class TTLManagementController {
    
    private final TTLManagementService ttlManagementService;
    
    public TTLManagementController(TTLManagementService ttlManagementService) {
        this.ttlManagementService = ttlManagementService;
    }
    
    /**
     * GET /api/ttl/status - Get current TTL management status
     */
    @GetMapping("/api/ttl/status")
    public ResponseEntity<TTLManagementStatus> getStatus() {
        return ResponseEntity.ok(ttlManagementService.getStatus());
    }
    
    /**
     * POST /api/ttl/enable - Enable all TTL rules globally
     */
    @PostMapping("/api/ttl/enable")
    public ResponseEntity<Map<String, Object>> enableAllTTL(@RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "REST API call");
        
        ttlManagementService.enableAllTTL(reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All TTL rules enabled");
        response.put("reason", reason);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/ttl/disable - Disable all TTL rules globally
     */
    @PostMapping("/api/ttl/disable")
    public ResponseEntity<Map<String, Object>> disableAllTTL(@RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "REST API call");
        
        ttlManagementService.disableAllTTL(reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All TTL rules disabled");
        response.put("reason", reason);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/ttl/extend - Extend all TTL globally by specified days
     */
    @PostMapping("/api/ttl/extend")
    public ResponseEntity<Map<String, Object>> extendGlobalTTL(@RequestBody Map<String, Object> request) {
        Integer extraDays = (Integer) request.get("extraDays");
        String reason = (String) request.getOrDefault("reason", "REST API call");
        
        if (extraDays == null || extraDays < 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "extraDays must be a positive integer");
            return ResponseEntity.badRequest().body(error);
        }
        
        ttlManagementService.extendGlobalTTL(extraDays, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Global TTL extended by " + extraDays + " days");
        response.put("extraDays", extraDays);
        response.put("reason", reason);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/ttl/class/{className}/override - Override TTL for a specific class
     */
    @PostMapping("/api/ttl/class/{className}/override")
    public ResponseEntity<Map<String, Object>> overrideClassTTL(
            @PathVariable String className,
            @RequestBody Map<String, Object> request) {
        
        try {
            Class<?> clazz = Class.forName(className);
            String overrideType = (String) request.get("overrideType");
            String reason = (String) request.getOrDefault("reason", "REST API call");
            
            TTLOverride override = createOverrideFromRequest(overrideType, request);
            if (override == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid override type: " + overrideType);
                return ResponseEntity.badRequest().body(error);
            }
            
            ttlManagementService.overrideClassTTL(clazz, override, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Class TTL overridden for " + className);
            response.put("overrideType", overrideType);
            response.put("reason", reason);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (ClassNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Class not found: " + className);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * POST /api/ttl/method/{className}/{methodName}/override - Override TTL for a specific method
     */
    @PostMapping("/api/ttl/method/{className}/{methodName}/override")
    public ResponseEntity<Map<String, Object>> overrideMethodTTL(
            @PathVariable String className,
            @PathVariable String methodName,
            @RequestBody Map<String, Object> request) {
        
        try {
            Class<?> clazz = Class.forName(className);
            String overrideType = (String) request.get("overrideType");
            String reason = (String) request.getOrDefault("reason", "REST API call");
            
            TTLOverride override = createOverrideFromRequest(overrideType, request);
            if (override == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid override type: " + overrideType);
                return ResponseEntity.badRequest().body(error);
            }
            
            ttlManagementService.overrideMethodTTL(clazz, methodName, override, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Method TTL overridden for " + className + "#" + methodName);
            response.put("overrideType", overrideType);
            response.put("reason", reason);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (ClassNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Class not found: " + className);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * POST /api/ttl/schedule - Schedule a TTL override with automatic removal
     */
    @PostMapping("/api/ttl/schedule")
    public ResponseEntity<Map<String, Object>> scheduleTTLOverride(@RequestBody Map<String, Object> request) {
        String className = (String) request.get("className");
        String overrideType = (String) request.get("overrideType");
        Long durationMs = (Long) request.get("durationMs");
        String reason = (String) request.getOrDefault("reason", "REST API call");
        
        if (className == null || overrideType == null || durationMs == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "className, overrideType, and durationMs are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Class<?> clazz = Class.forName(className);
            TTLOverride override = createOverrideFromRequest(overrideType, request);
            
            if (override == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid override type: " + overrideType);
                return ResponseEntity.badRequest().body(error);
            }
            
            String overrideId = UUID.randomUUID().toString();
            ttlManagementService.scheduleTTLOverride(overrideId, clazz, override, durationMs, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "TTL override scheduled for " + className);
            response.put("overrideId", overrideId);
            response.put("overrideType", overrideType);
            response.put("durationMs", durationMs);
            response.put("reason", reason);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (ClassNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Class not found: " + className);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * DELETE /api/ttl/class/{className}/override - Remove TTL override for a class
     */
    @DeleteMapping("/api/ttl/class/{className}/override")
    public ResponseEntity<Map<String, Object>> removeClassTTLOverride(
            @PathVariable String className,
            @RequestParam(defaultValue = "REST API call") String reason) {
        
        try {
            Class<?> clazz = Class.forName(className);
            ttlManagementService.removeScheduledOverride("", clazz, reason); // This will remove the override
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Class TTL override removed for " + className);
            response.put("reason", reason);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (ClassNotFoundException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Class not found: " + className);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * DELETE /api/ttl/clear - Clear all TTL overrides
     */
    @DeleteMapping("/api/ttl/clear")
    public ResponseEntity<Map<String, Object>> clearAllOverrides(@RequestParam(defaultValue = "REST API call") String reason) {
        ttlManagementService.clearAllOverrides(reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All TTL overrides cleared");
        response.put("reason", reason);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to create TTLOverride from request parameters
     */
    private TTLOverride createOverrideFromRequest(String overrideType, Map<String, Object> request) {
        switch (overrideType.toLowerCase()) {
            case "bypass":
                return TTLOverride.bypass();
                
            case "extend":
                Integer extraDays = (Integer) request.get("extraDays");
                if (extraDays == null || extraDays < 0) {
                    return null;
                }
                return TTLOverride.extend(extraDays);
                
            case "replace":
                String startDate = (String) request.get("startDate");
                Integer ttlDays = (Integer) request.get("ttlDays");
                String[] levelNames = (String[]) request.get("levels");
                
                if (startDate == null || ttlDays == null) {
                    return null;
                }
                
                LogLevel[] levels = null;
                if (levelNames != null) {
                    levels = new LogLevel[levelNames.length];
                    for (int i = 0; i < levelNames.length; i++) {
                        try {
                            levels[i] = LogLevel.valueOf(levelNames[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null; // Invalid log level
                        }
                    }
                }
                
                return TTLOverride.replace(startDate, ttlDays, levels);
                
            default:
                return null;
        }
    }
}

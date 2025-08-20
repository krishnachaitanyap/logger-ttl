package com.logger.ttl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TTLManager runtime TTL management functionality
 */
@DisplayName("TTLManager Runtime TTL Management")
class TTLManagerTest {
    
    private TTLManager manager;
    private TTLConfig testConfig;
    
    @BeforeEach
    void setUp() {
        manager = TTLManager.getInstance();
        manager.clearAllOverrides(); // Reset to clean state
        testConfig = new TTLConfig("2025-01-01T00:00:00Z", 30, new LogLevel[]{LogLevel.DEBUG, LogLevel.INFO});
    }
    
    @Test
    @DisplayName("Global TTL override should bypass all TTL rules")
    void testGlobalTTLOverride() {
        // Initially TTL should be enforced
        assertFalse(manager.isGlobalTTLEnabled());
        
        // Enable global override
        manager.enableAllTTL();
        assertTrue(manager.isGlobalTTLEnabled());
        
        // Should bypass TTL for any context
        assertTrue(manager.shouldBypassTTL(String.class, "test", "field", testConfig));
        
        // Disable global override
        manager.disableAllTTL();
        assertFalse(manager.isGlobalTTLEnabled());
        assertFalse(manager.shouldBypassTTL(String.class, "test", "field", testConfig));
    }
    
    @Test
    @DisplayName("Global TTL extension should add extra days to all TTL calculations")
    void testGlobalTTLExtension() {
        // Set global extension
        manager.setGlobalTTLExtension(15);
        assertEquals(15, manager.getGlobalTTLExtension());
        
        // Apply extension to test config
        TTLConfig extendedConfig = manager.applyGlobalExtension(testConfig);
        assertEquals(45, extendedConfig.getTtlDays()); // 30 + 15
        
        // Remove extension
        manager.setGlobalTTLExtension(0);
        assertEquals(0, manager.getGlobalTTLExtension());
        
        // Config should be back to original
        TTLConfig originalConfig = manager.applyGlobalExtension(testConfig);
        assertEquals(30, originalConfig.getTtlDays());
    }
    
    @Test
    @DisplayName("Class-level TTL override should work correctly")
    void testClassTTLOverride() {
        // Create bypass override for String class
        TTLOverride override = TTLOverride.bypass();
        manager.overrideClassTTL(String.class, override);
        
        // Should bypass TTL for String class
        assertTrue(manager.shouldBypassTTL(String.class, "test", "field", testConfig));
        
        // Should not bypass for other classes
        assertFalse(manager.shouldBypassTTL(Integer.class, "test", "field", testConfig));
        
        // Remove override
        manager.removeClassTTLOverride(String.class);
        assertFalse(manager.shouldBypassTTL(String.class, "test", "field", testConfig));
    }
    
    @Test
    @DisplayName("Method-level TTL override should work correctly")
    void testMethodTTLOverride() {
        // Create extend override for specific method
        TTLOverride override = TTLOverride.extend(20);
        manager.overrideMethodTTL(String.class, "testMethod", override);
        
        // Should not bypass TTL (extend doesn't bypass)
        assertFalse(manager.shouldBypassTTL(String.class, "testMethod", "field", testConfig));
        
        // Should not bypass for other methods
        assertFalse(manager.shouldBypassTTL(String.class, "otherMethod", "field", testConfig));
        
        // Remove override
        manager.removeMethodTTLOverride(String.class, "testMethod");
        assertFalse(manager.shouldBypassTTL(String.class, "testMethod", "field", testConfig));
    }
    
    @Test
    @DisplayName("Field-level TTL override should work correctly")
    void testFieldTTLOverride() {
        // Create replace override for specific field
        TTLOverride override = TTLOverride.replace("2025-12-31T23:59:59Z", 365, LogLevel.ERROR);
        manager.overrideFieldTTL(String.class, "testField", override);
        
        // Should not bypass TTL (replace doesn't bypass)
        assertFalse(manager.shouldBypassTTL(String.class, "test", "testField", testConfig));
        
        // Should not bypass for other fields
        assertFalse(manager.shouldBypassTTL(String.class, "test", "otherField", testConfig));
        
        // Remove override
        manager.removeFieldTTLOverride(String.class, "testField");
        assertFalse(manager.shouldBypassTTL(String.class, "test", "testField", testConfig));
    }
    
    @Test
    @DisplayName("TTLOverride.bypass should always bypass TTL")
    void testTTLOverrideBypass() {
        TTLOverride bypass = TTLOverride.bypass();
        
        assertTrue(bypass.shouldBypass(testConfig));
        
        TTLConfig result = bypass.apply(testConfig);
        assertEquals(-1, result.getTtlDays()); // Never expires
        assertFalse(result.hasTTL());
    }
    
    @Test
    @DisplayName("TTLOverride.extend should add extra days to TTL")
    void testTTLOverrideExtend() {
        TTLOverride extend = TTLOverride.extend(25);
        
        assertFalse(extend.shouldBypass(testConfig));
        
        TTLConfig result = extend.apply(testConfig);
        assertEquals(55, result.getTtlDays()); // 30 + 25
        assertTrue(result.hasTTL());
    }
    
    @Test
    @DisplayName("TTLOverride.replace should use new configuration")
    void testTTLOverrideReplace() {
        TTLOverride replace = TTLOverride.replace("2025-06-01T00:00:00Z", 60, LogLevel.WARN, LogLevel.ERROR);
        
        assertFalse(replace.shouldBypass(testConfig));
        
        TTLConfig result = replace.apply(testConfig);
        assertEquals("2025-06-01T00:00:00Z", result.getStartTime().toString());
        assertEquals(60, result.getTtlDays());
        assertTrue(result.isLevelAffected(LogLevel.WARN));
        assertTrue(result.isLevelAffected(LogLevel.ERROR));
        assertFalse(result.isLevelAffected(LogLevel.INFO));
    }
    
    @Test
    @DisplayName("Clear all overrides should reset manager to initial state")
    void testClearAllOverrides() {
        // Set up various overrides
        manager.enableAllTTL();
        manager.setGlobalTTLExtension(10);
        manager.overrideClassTTL(String.class, TTLOverride.bypass());
        manager.overrideMethodTTL(String.class, "test", TTLOverride.extend(5));
        manager.overrideFieldTTL(String.class, "field", TTLOverride.replace("2025-12-31T00:00:00Z", 100));
        
        // Verify overrides are set
        assertTrue(manager.isGlobalTTLEnabled());
        assertEquals(10, manager.getGlobalTTLExtension());
        assertNotNull(manager.getClassTTLOverride(String.class));
        assertNotNull(manager.getMethodTTLOverride(String.class, "test"));
        assertNotNull(manager.getFieldTTLOverride(String.class, "field"));
        
        // Clear all overrides
        manager.clearAllOverrides();
        
        // Verify everything is reset
        assertFalse(manager.isGlobalTTLEnabled());
        assertEquals(0, manager.getGlobalTTLExtension());
        assertNull(manager.getClassTTLOverride(String.class));
        assertNull(manager.getMethodTTLOverride(String.class, "test"));
        assertNull(manager.getFieldTTLOverride(String.class, "field"));
    }
    
    @Test
    @DisplayName("Get overrides summary should provide current status")
    void testGetOverridesSummary() {
        String summary = manager.getOverridesSummary();
        
        assertTrue(summary.contains("TTL Manager Status:"));
        assertTrue(summary.contains("Global TTL Override: false"));
        assertTrue(summary.contains("Global TTL Extension: +0 days"));
        assertTrue(summary.contains("Class Overrides: 0"));
        assertTrue(summary.contains("Method Overrides: 0"));
        assertTrue(summary.contains("Field Overrides: 0"));
        
        // Add some overrides and check summary
        manager.enableAllTTL();
        manager.setGlobalTTLExtension(25);
        manager.overrideClassTTL(String.class, TTLOverride.bypass());
        
        String updatedSummary = manager.getOverridesSummary();
        assertTrue(updatedSummary.contains("Global TTL Override: true"));
        assertTrue(updatedSummary.contains("Global TTL Extension: +25 days"));
        assertTrue(updatedSummary.contains("Class Overrides: 1"));
    }
    
    @Test
    @DisplayName("TTLOverride should handle null and edge cases gracefully")
    void testTTLOverrideEdgeCases() {
        // Test with null config
        TTLOverride bypass = TTLOverride.bypass();
        assertDoesNotThrow(() -> bypass.apply(null));
        
        // Test with empty levels
        TTLOverride replace = TTLOverride.replace("2025-01-01T00:00:00Z", 30);
        TTLConfig result = replace.apply(testConfig);
        assertTrue(result.getLevels().isEmpty()); // All levels affected
        
        // Test with zero TTL
        TTLOverride extend = TTLOverride.extend(0);
        TTLConfig extended = extend.apply(testConfig);
        assertEquals(30, extended.getTtlDays()); // No change
    }
    
    @Test
    @DisplayName("TTLManager should be thread-safe singleton")
    void testThreadSafety() {
        TTLManager instance1 = TTLManager.getInstance();
        TTLManager instance2 = TTLManager.getInstance();
        
        // Should be the same instance
        assertSame(instance1, instance2);
        
        // Should be thread-safe
        assertDoesNotThrow(() -> {
            Thread thread = new Thread(() -> {
                TTLManager threadInstance = TTLManager.getInstance();
                threadInstance.enableAllTTL();
                assertTrue(threadInstance.isGlobalTTLEnabled());
            });
            thread.start();
            thread.join();
        });
    }
}

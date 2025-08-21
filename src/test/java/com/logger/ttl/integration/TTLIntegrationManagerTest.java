package com.logger.ttl.integration;

import com.logger.ttl.TTLManager;
import com.logger.ttl.TTLOverride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TTLIntegrationManager Non-Spring Integration")
class TTLIntegrationManagerTest {
    
    private TTLIntegrationManager integrationManager;
    private TTLManager ttlManager;
    
    @BeforeEach
    void setUp() {
        integrationManager = TTLIntegrationManager.getInstance();
        ttlManager = TTLManager.getInstance();
        
        // Clear any existing state
        ttlManager.clearAllOverrides();
    }
    
    @Test
    @DisplayName("Should be singleton instance")
    void testSingletonInstance() {
        TTLIntegrationManager instance1 = TTLIntegrationManager.getInstance();
        TTLIntegrationManager instance2 = TTLIntegrationManager.getInstance();
        
        assertSame(instance1, instance2, "Should return same instance");
    }
    
    @Test
    @DisplayName("Should add and remove event listeners")
    void testEventListenerManagement() {
        TTLEventListener listener = event -> {};
        
        int initialCount = integrationManager.getEventListenerCount();
        
        integrationManager.addEventListener(listener);
        assertEquals(initialCount + 1, integrationManager.getEventListenerCount(), 
            "Should increase listener count");
        
        integrationManager.removeEventListener(listener);
        assertEquals(initialCount, integrationManager.getEventListenerCount(), 
            "Should decrease listener count");
    }
    
    @Test
    @DisplayName("Should add lambda event listeners")
    void testLambdaEventListener() {
        int initialCount = integrationManager.getEventListenerCount();
        
        integrationManager.addLambdaEventListener(event -> {});
        
        assertEquals(initialCount + 1, integrationManager.getEventListenerCount(), 
            "Should add lambda event listener");
    }
    
    @Test
    @DisplayName("Should enable all TTL rules")
    void testEnableAllTTL() {
        integrationManager.enableAllTTL("Test reason");
        
        assertTrue(ttlManager.isGlobalTTLEnabled(), "Global TTL should be enabled");
    }
    
    @Test
    @DisplayName("Should disable all TTL rules")
    void testDisableAllTTL() {
        // First enable
        integrationManager.enableAllTTL("Test enable");
        assertTrue(ttlManager.isGlobalTTLEnabled(), "Global TTL should be enabled");
        
        // Then disable
        integrationManager.disableAllTTL("Test disable");
        assertFalse(ttlManager.isGlobalTTLEnabled(), "Global TTL should be disabled");
    }
    
    @Test
    @DisplayName("Should extend global TTL")
    void testExtendGlobalTTL() {
        integrationManager.extendGlobalTTL(5, "Test extension");
        
        assertEquals(5, ttlManager.getGlobalTTLExtension(), 
            "Global TTL extension should be 5 days");
    }
    
    @Test
    @DisplayName("Should override class TTL")
    void testOverrideClassTTL() {
        TTLOverride override = TTLOverride.bypass();
        Class<?> testClass = TTLIntegrationManagerTest.class;
        
        integrationManager.overrideClassTTL(testClass, override, "Test override");
        
        TTLOverride retrievedOverride = ttlManager.getClassTTLOverride(testClass);
        assertNotNull(retrievedOverride, "Should retrieve class TTL override");
        assertEquals(override, retrievedOverride, "Should match the set override");
    }
    
    @Test
    @DisplayName("Should override method TTL")
    void testOverrideMethodTTL() {
        TTLOverride override = TTLOverride.extend(10);
        Class<?> testClass = TTLIntegrationManagerTest.class;
        String methodName = "testOverrideMethodTTL";
        
        integrationManager.overrideMethodTTL(testClass, methodName, override, "Test method override");
        
        TTLOverride retrievedOverride = ttlManager.getMethodTTLOverride(testClass, methodName);
        assertNotNull(retrievedOverride, "Should retrieve method TTL override");
        assertEquals(override, retrievedOverride, "Should match the set override");
    }
    
    @Test
    @DisplayName("Should override field TTL")
    void testOverrideFieldTTL() {
        TTLOverride override = TTLOverride.replace("2025-01-01T00:00:00Z", 30);
        Class<?> testClass = TTLIntegrationManagerTest.class;
        String fieldName = "integrationManager";
        
        integrationManager.overrideFieldTTL(testClass, fieldName, override, "Test field override");
        
        TTLOverride retrievedOverride = ttlManager.getFieldTTLOverride(testClass, fieldName);
        assertNotNull(retrievedOverride, "Should retrieve field TTL override");
        assertEquals(override, retrievedOverride, "Should match the set override");
    }
    
    @Test
    @DisplayName("Should clear all overrides")
    void testClearAllOverrides() {
        // Set some overrides first
        TTLOverride override = TTLOverride.bypass();
        Class<?> testClass = TTLIntegrationManagerTest.class;
        
        integrationManager.overrideClassTTL(testClass, override, "Test override");
        assertNotNull(ttlManager.getClassTTLOverride(testClass), "Override should exist");
        
        // Clear all overrides
        integrationManager.clearAllOverrides("Test clear");
        
        assertNull(ttlManager.getClassTTLOverride(testClass), "Override should be cleared");
    }
    
    @Test
    @DisplayName("Should schedule and manage overrides")
    void testScheduledOverrides() {
        TTLOverride override = TTLOverride.bypass();
        Class<?> testClass = TTLIntegrationManagerTest.class;
        String overrideId = "test-scheduled-override";
        
        // Schedule override
        integrationManager.scheduleOverride(overrideId, testClass, override, 1000, "Test scheduled");
        
        assertTrue(integrationManager.getScheduledOverrideIds().contains(overrideId), 
            "Should contain scheduled override ID");
        assertEquals(1, integrationManager.getScheduledOverrideCount(), 
            "Should have one scheduled override");
        
        // Wait for override to expire
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Override should be automatically removed
        assertEquals(0, integrationManager.getScheduledOverrideCount(), 
            "Scheduled override should be automatically removed");
    }
    
    @Test
    @DisplayName("Should get integration status")
    void testGetStatus() {
        TTLManagementStatus status = integrationManager.getStatus();
        
        assertNotNull(status, "Status should not be null");
        assertNotNull(status.getOverridesSummary(), "Overrides summary should not be null");
        assertTrue(status.getListenerCount() >= 0, "Listener count should be non-negative");
    }
    
    @Test
    @DisplayName("Should track operation count")
    void testOperationCount() {
        long initialCount = integrationManager.getTotalOperations();
        
        integrationManager.enableAllTTL("Test operation");
        
        assertTrue(integrationManager.getTotalOperations() > initialCount, 
            "Operation count should increase");
    }
    
    @Test
    @DisplayName("Should provide access to TTL manager")
    void testTTLManagerAccess() {
        TTLManager retrievedManager = integrationManager.getTTLManager();
        
        assertNotNull(retrievedManager, "TTL manager should not be null");
        assertSame(ttlManager, retrievedManager, "Should return same TTL manager instance");
    }
    
    @Test
    @DisplayName("Should provide access to event publisher")
    void testEventPublisherAccess() {
        TTLEventPublisher eventPublisher = integrationManager.getEventPublisher();
        
        assertNotNull(eventPublisher, "Event publisher should not be null");
        assertTrue(eventPublisher.isEnabled(), "Event publisher should be enabled");
    }
    
    @Test
    @DisplayName("Should handle utility methods correctly")
    void testUtilityMethods() {
        // Test shouldBypassTTL
        boolean shouldBypass = integrationManager.shouldBypassTTL(
            TTLIntegrationManagerTest.class, "testMethod", "testField", null);
        assertFalse(shouldBypass, "Should not bypass TTL by default");
        
        // Test applyGlobalExtension
        // This would require a TTLConfig instance, but we can test the method exists
        assertNull(integrationManager.getClassTTLOverride(TTLIntegrationManagerTest.class), 
            "Should return null for non-existent override");
    }
}

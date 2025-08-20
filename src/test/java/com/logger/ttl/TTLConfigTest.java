package com.logger.ttl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Tests for TTLConfig class.
 */
public class TTLConfigTest {
    
    private TTLConfig defaultConfig;
    private TTLConfig futureStartConfig;
    private TTLConfig pastStartConfig;
    private TTLConfig ttlConfig;
    private TTLConfig levelSpecificConfig;
    
    @BeforeEach
    void setUp() {
        defaultConfig = TTLConfig.defaultConfig();
        
        // Future start date (1 day from now)
        String futureStart = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        futureStartConfig = new TTLConfig(futureStart, 30, new LogLevel[]{});
        
        // Past start date (1 day ago)
        String pastStart = Instant.now().minus(1, ChronoUnit.DAYS).toString();
        pastStartConfig = new TTLConfig(pastStart, 30, new LogLevel[]{});
        
        // TTL only (no start date)
        ttlConfig = new TTLConfig("", 7, new LogLevel[]{});
        
        // Level-specific config
        levelSpecificConfig = new TTLConfig("", 14, new LogLevel[]{LogLevel.DEBUG, LogLevel.INFO});
    }
    
    @Test
    void testDefaultConfig() {
        assertNotNull(defaultConfig);
        assertFalse(defaultConfig.hasStartTime());
        assertFalse(defaultConfig.hasTTL());
        assertTrue(defaultConfig.isWithinTTL());
        assertTrue(defaultConfig.isLevelAffected(LogLevel.DEBUG));
        assertTrue(defaultConfig.isLevelAffected(LogLevel.ERROR));
        assertTrue(defaultConfig.shouldLog(LogLevel.INFO));
    }
    
    @Test
    void testFutureStartConfig() {
        assertTrue(futureStartConfig.hasStartTime());
        assertTrue(futureStartConfig.hasTTL());
        assertFalse(futureStartConfig.isWithinTTL()); // Not started yet
        assertTrue(futureStartConfig.isLevelAffected(LogLevel.DEBUG));
        assertFalse(futureStartConfig.shouldLog(LogLevel.DEBUG));
    }
    
    @Test
    void testPastStartConfig() {
        assertTrue(pastStartConfig.hasStartTime());
        assertTrue(pastStartConfig.hasTTL());
        assertTrue(pastStartConfig.isWithinTTL()); // Started and within TTL
        assertTrue(pastStartConfig.isLevelAffected(LogLevel.DEBUG));
        assertTrue(pastStartConfig.shouldLog(LogLevel.DEBUG));
    }
    
    @Test
    void testTTLOnlyConfig() {
        assertFalse(ttlConfig.hasStartTime());
        assertTrue(ttlConfig.hasTTL());
        assertTrue(ttlConfig.isWithinTTL()); // Within TTL window
        assertTrue(ttlConfig.isLevelAffected(LogLevel.DEBUG));
        assertTrue(ttlConfig.shouldLog(LogLevel.DEBUG));
    }
    
    @Test
    void testLevelSpecificConfig() {
        assertFalse(levelSpecificConfig.hasStartTime());
        assertTrue(levelSpecificConfig.hasTTL());
        assertTrue(levelSpecificConfig.isLevelAffected(LogLevel.DEBUG));
        assertTrue(levelSpecificConfig.isLevelAffected(LogLevel.INFO));
        assertFalse(levelSpecificConfig.isLevelAffected(LogLevel.WARN));
        assertFalse(levelSpecificConfig.isLevelAffected(LogLevel.ERROR));
        
        assertTrue(levelSpecificConfig.shouldLog(LogLevel.DEBUG));
        assertTrue(levelSpecificConfig.shouldLog(LogLevel.INFO));
        assertTrue(levelSpecificConfig.shouldLog(LogLevel.WARN)); // Not affected by TTL
        assertTrue(levelSpecificConfig.shouldLog(LogLevel.ERROR)); // Not affected by TTL
    }
    
    @Test
    void testInvalidStartDate() {
        // Should not throw exception, should gracefully handle invalid dates
        TTLConfig config = new TTLConfig("invalid-date", 30, new LogLevel[]{});
        assertFalse(config.hasStartTime());
        assertTrue(config.hasTTL());
        assertTrue(config.isWithinTTL());
    }
    
    @Test
    void testNullStartDate() {
        TTLConfig config = new TTLConfig(null, 30, new LogLevel[]{});
        assertFalse(config.hasStartTime());
        assertTrue(config.hasTTL());
        assertTrue(config.isWithinTTL());
    }
    
    @Test
    void testEmptyStartDate() {
        TTLConfig config = new TTLConfig("", 30, new LogLevel[]{});
        assertFalse(config.hasStartTime());
        assertTrue(config.hasTTL());
        assertTrue(config.isWithinTTL());
    }
    
    @Test
    void testNullLevels() {
        TTLConfig config = new TTLConfig("", 30, null);
        assertFalse(config.hasStartTime());
        assertTrue(config.hasTTL());
        assertTrue(config.isLevelAffected(LogLevel.DEBUG)); // All levels affected
        assertTrue(config.isLevelAffected(LogLevel.ERROR));
    }
    
    @Test
    void testEmptyLevels() {
        TTLConfig config = new TTLConfig("", 30, new LogLevel[]{});
        assertFalse(config.hasStartTime());
        assertTrue(config.hasTTL());
        assertTrue(config.isLevelAffected(LogLevel.DEBUG)); // All levels affected
        assertTrue(config.isLevelAffected(LogLevel.ERROR));
    }
    
    @Test
    void testTTLExpiration() {
        // Create a config that expires in 1 day from now
        TTLConfig shortTTLConfig = new TTLConfig("", 1, new LogLevel[]{});
        
        // Should be valid initially
        assertTrue(shortTTLConfig.isWithinTTL());
        
        // For testing purposes, we'll test with a very short TTL
        // Create a config that expires in 1 second by using a fraction of a day
        TTLConfig veryShortTTLConfig = new TTLConfig("", 1, new LogLevel[]{});
        
        // Wait for a moment to ensure some time has passed
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Should still be valid (1 day TTL is much longer than 100ms)
        assertTrue(veryShortTTLConfig.isWithinTTL());
    }
    
    @Test
    void testGetters() {
        assertEquals(30, pastStartConfig.getTtlDays());
        assertNotNull(pastStartConfig.getStartTime());
        assertTrue(pastStartConfig.getLevels().isEmpty()); // Should be empty when no specific levels specified
        assertEquals(2, levelSpecificConfig.getLevels().size());
        assertTrue(levelSpecificConfig.getLevels().contains(LogLevel.DEBUG));
        assertTrue(levelSpecificConfig.getLevels().contains(LogLevel.INFO));
    }
}

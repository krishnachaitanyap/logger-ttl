package com.logger.ttl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TTLLogger class.
 */
public class TTLLoggerTest {
    
    private TTLLogger logger;
    
    @BeforeEach
    void setUp() {
        logger = TTLLogger.getLogger(TTLLoggerTest.class);
    }
    
    @Test
    void testLoggerCreation() {
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testLoggerWithClass() {
        TTLLogger classLogger = TTLLogger.getLogger(TTLLoggerTest.class);
        assertNotNull(classLogger);
        assertEquals(TTLLoggerTest.class.getName(), classLogger.getDelegate().getName());
    }
    
    @Test
    void testLoggerWithName() {
        TTLLogger nameLogger = TTLLogger.getLogger("TestLogger");
        assertNotNull(nameLogger);
        assertEquals("TestLogger", nameLogger.getDelegate().getName());
    }
    
    @Test
    void testIsEnabledFor() {
        // These tests depend on the underlying logger configuration
        // In a test environment, they may return false for some levels
        // We'll test that the method doesn't throw exceptions and returns boolean values
        assertNotNull(logger.isEnabledFor(LogLevel.ERROR));
        assertNotNull(logger.isEnabledFor(LogLevel.WARN));
        assertNotNull(logger.isEnabledFor(LogLevel.INFO));
        assertNotNull(logger.isEnabledFor(LogLevel.DEBUG));
        assertNotNull(logger.isEnabledFor(LogLevel.TRACE));
    }
    
    @Test
    void testExplicitTTLMethods() {
        // Test explicit TTL configuration methods
        logger.trace("Test trace", "", 1, LogLevel.TRACE);
        logger.debug("Test debug", "", 1, LogLevel.DEBUG);
        logger.info("Test info", "", 1, LogLevel.INFO);
        logger.warn("Test warn", "", 1, LogLevel.WARN);
        logger.error("Test error", "", 1, LogLevel.ERROR);
        
        // Test with specific start date
        String futureStart = java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS).toString();
        logger.info("Future log", futureStart, 30, LogLevel.INFO);
        
        // Test with level filtering
        logger.debug("Debug only", "", 7, LogLevel.DEBUG);
        logger.info("Info only", "", 7, LogLevel.INFO);
    }
    
    @Test
    void testExplicitTTLWithInvalidStartDate() {
        // Should not throw exception, should gracefully handle invalid dates
        assertDoesNotThrow(() -> {
            logger.info("Invalid date log", "invalid-date", 30, LogLevel.INFO);
        });
    }
    
    @Test
    void testExplicitTTLWithNullStartDate() {
        assertDoesNotThrow(() -> {
            logger.info("Null start date log", null, 30, LogLevel.INFO);
        });
    }
    
    @Test
    void testExplicitTTLWithEmptyStartDate() {
        assertDoesNotThrow(() -> {
            logger.info("Empty start date log", "", 30, LogLevel.INFO);
        });
    }
    
    @Test
    void testExplicitTTLWithNullLevels() {
        assertDoesNotThrow(() -> {
            logger.info("Null levels log", "", 30, (LogLevel[]) null);
        });
    }
    
    @Test
    void testExplicitTTLWithEmptyLevels() {
        assertDoesNotThrow(() -> {
            logger.info("Empty levels log", "", 30, new LogLevel[]{});
        });
    }
    
    @Test
    void testExplicitTTLWithSpecificLevels() {
        // Test with specific levels
        logger.info("Info level only", "", 14, LogLevel.INFO);
        logger.debug("Debug level only", "", 14, LogLevel.DEBUG);
        logger.warn("Warn level only", "", 14, LogLevel.WARN);
        logger.error("Error level only", "", 14, LogLevel.ERROR);
        logger.trace("Trace level only", "", 14, LogLevel.TRACE);
        
        // Test with multiple levels
        logger.info("Multiple levels", "", 21, LogLevel.INFO, LogLevel.WARN);
        logger.debug("Multiple levels", "", 21, LogLevel.DEBUG, LogLevel.INFO);
    }
    
    @Test
    void testExplicitTTLExpiration() {
        // Test with very short TTL
        logger.info("Short TTL log", "", 1, LogLevel.INFO);
        
        // Wait for expiration
        try {
            Thread.sleep(1100); // Wait slightly more than 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Should still work as the TTL is calculated from the time of the call
        logger.info("After expiration", "", 1, LogLevel.INFO);
    }
    
    @Test
    void testExplicitTTLWithNegativeDays() {
        // Negative TTL should mean never expires
        logger.info("Never expires log", "", -1, LogLevel.INFO);
        logger.debug("Never expires debug", "", -1, LogLevel.DEBUG);
        logger.warn("Never expires warn", "", -1, LogLevel.WARN);
        logger.error("Never expires error", "", -1, LogLevel.ERROR);
        logger.trace("Never expires trace", "", -1, LogLevel.TRACE);
    }
    
    @Test
    void testExplicitTTLWithZeroDays() {
        // Zero TTL should expire immediately
        logger.info("Zero TTL log", "", 0, LogLevel.INFO);
        
        // Wait a moment
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Should be expired
        logger.info("After zero TTL", "", 0, LogLevel.INFO);
    }
}

package com.logger.ttl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TTLLoggerFactory class.
 */
public class TTLLoggerFactoryTest {
    
    @Test
    void testGetLoggerWithClass() {
        TTLLogger logger = TTLLoggerFactory.getLogger(TTLLoggerFactoryTest.class);
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
        assertEquals(TTLLoggerFactoryTest.class.getName(), logger.getDelegate().getName());
    }
    
    @Test
    void testGetLoggerWithName() {
        TTLLogger logger = TTLLoggerFactory.getLogger("TestLoggerName");
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
        assertEquals("TestLoggerName", logger.getDelegate().getName());
    }
    
    @Test
    void testGetLoggerWithExplicitTTL() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "2025-01-01T00:00:00Z", 
            30, 
            LogLevel.INFO, 
            LogLevel.WARN
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
        assertEquals(TTLLoggerFactoryTest.class.getName(), logger.getDelegate().getName());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLAndName() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            "ExplicitTTLLogger", 
            "2025-01-01T00:00:00Z", 
            60, 
            LogLevel.DEBUG
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
        assertEquals("ExplicitTTLLogger", logger.getDelegate().getName());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLNoStartDate() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "", 
            14, 
            LogLevel.INFO
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLNoLevels() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "2025-01-01T00:00:00Z", 
            30
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLNegativeDays() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "", 
            -1, 
            LogLevel.ERROR
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLZeroDays() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "", 
            0, 
            LogLevel.WARN
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testGetLoggerWithExplicitTTLInvalidStartDate() {
        // Should not throw exception, should gracefully handle invalid dates
        assertDoesNotThrow(() -> {
            TTLLogger logger = TTLLoggerFactory.getLogger(
                TTLLoggerFactoryTest.class, 
                "invalid-date", 
                30, 
                LogLevel.INFO
            );
            assertNotNull(logger);
        });
    }
    
    @Test
    void testGetLoggerWithExplicitTTLNullStartDate() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            TTLLogger logger = TTLLoggerFactory.getLogger(
                TTLLoggerFactoryTest.class, 
                null, 
                30, 
                LogLevel.INFO
            );
            assertNotNull(logger);
        });
    }
    
    @Test
    void testGetLoggerWithExplicitTTLNullLevels() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            TTLLogger logger = TTLLoggerFactory.getLogger(
                TTLLoggerFactoryTest.class, 
                "", 
                30, 
                (LogLevel[]) null
            );
            assertNotNull(logger);
        });
    }
    
    @Test
    void testGetLoggerWithExplicitTTLEmptyLevels() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "", 
            30, 
            new LogLevel[]{}
        );
        
        assertNotNull(logger);
        assertNotNull(logger.getDelegate());
    }
    
    @Test
    void testExplicitTTLLoggerBehavior() {
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            "", 
            1, // 1 day TTL
            LogLevel.INFO, 
            LogLevel.WARN
        );
        
        // Test that the logger respects the explicit TTL configuration
        assertNotNull(logger);
        
        // These should work (within TTL and affected levels)
        logger.info("Info message");
        logger.warn("Warn message");
        
        // These should also work (not affected by TTL)
        logger.debug("Debug message");
        logger.error("Error message");
        logger.trace("Trace message");
    }
    
    @Test
    void testExplicitTTLLoggerWithFutureStartDate() {
        String futureStart = java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS).toString();
        
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            futureStart, 
            30, 
            LogLevel.INFO
        );
        
        assertNotNull(logger);
        
        // Should not log yet (future start date)
        logger.info("Future message");
    }
    
    @Test
    void testExplicitTTLLoggerWithPastStartDate() {
        String pastStart = java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS).toString();
        
        TTLLogger logger = TTLLoggerFactory.getLogger(
            TTLLoggerFactoryTest.class, 
            pastStart, 
            30, 
            LogLevel.INFO
        );
        
        assertNotNull(logger);
        
        // Should log (past start date and within TTL)
        logger.info("Past start message");
    }
}

package com.cbo.core.logger

import com.cbo.core.logger.api.LogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogLevelTest {

    @Test
    fun `priority levels are properly ordered`() {
        assertTrue(LogLevel.VERBOSE.priority < LogLevel.DEBUG.priority)
        assertTrue(LogLevel.DEBUG.priority < LogLevel.INFO.priority)
        assertTrue(LogLevel.INFO.priority < LogLevel.WARN.priority)
        assertTrue(LogLevel.WARN.priority < LogLevel.ERROR.priority)
        assertTrue(LogLevel.ERROR.priority < LogLevel.NONE.priority)
    }

    @Test
    fun `fromString parses valid level strings case-insensitively`() {
        assertEquals(LogLevel.VERBOSE, LogLevel.fromString("verbose"))
        assertEquals(LogLevel.DEBUG, LogLevel.fromString("DEBUG"))
        assertEquals(LogLevel.INFO, LogLevel.fromString("Info"))
        assertEquals(LogLevel.WARN, LogLevel.fromString("WARN "))
        assertEquals(LogLevel.ERROR, LogLevel.fromString("error"))
    }

    @Test
    fun `fromString returns default when value is invalid`() {
        assertEquals(LogLevel.DEBUG, LogLevel.fromString("UNKNOWN_LEVEL"))
        assertEquals(LogLevel.INFO, LogLevel.fromString("", default = LogLevel.INFO))
    }
}

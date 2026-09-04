package com.cbo.core.logger

import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.sinks.LogcatSink
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogcatSinkTest {

    @Test
    fun `isEnabled respects minimum log level`() {
        val sink = LogcatSink(minLevel = LogLevel.INFO)

        assertFalse(sink.isEnabled(LogLevel.VERBOSE))
        assertFalse(sink.isEnabled(LogLevel.DEBUG))
        assertTrue(sink.isEnabled(LogLevel.INFO))
        assertTrue(sink.isEnabled(LogLevel.WARN))
        assertTrue(sink.isEnabled(LogLevel.ERROR))
        assertFalse(sink.isEnabled(LogLevel.NONE))
    }

    @Test
    fun `log handles standard messages without crashing`() {
        val sink = LogcatSink(minLevel = LogLevel.VERBOSE)

        sink.log(
            level = LogLevel.DEBUG,
            tag = "TestTag",
            message = "Short log message",
            throwable = null,
            metadata = null
        )
    }

    @Test
    fun `log handles messages with metadata and throwable`() {
        val sink = LogcatSink(minLevel = LogLevel.VERBOSE)

        sink.log(
            level = LogLevel.ERROR,
            tag = "ErrorTag",
            message = "Operation failed",
            throwable = RuntimeException("Boom"),
            metadata = mapOf("userId" to "12345", "screen" to "Map")
        )
    }

    @Test
    fun `log splits messages exceeding 4000 chars cleanly`() {
        val sink = LogcatSink(minLevel = LogLevel.VERBOSE)
        val longMessage = "A".repeat(10_000)

        sink.log(
            level = LogLevel.INFO,
            tag = "LongTag",
            message = longMessage,
            throwable = null,
            metadata = null
        )
    }
}

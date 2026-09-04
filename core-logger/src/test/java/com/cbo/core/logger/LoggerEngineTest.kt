package com.cbo.core.logger

import com.cbo.core.logger.api.DefaultLoggerConfigProvider
import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink
import com.cbo.core.logger.api.LoggerConfig
import com.cbo.core.logger.engine.LoggerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggerEngineTest {

    private class TestSink(val minLevel: LogLevel = LogLevel.DEBUG) : LogSink {
        val loggedEntries = mutableListOf<LoggedItem>()
        var flushCalled = false

        data class LoggedItem(
            val level: LogLevel,
            val tag: String,
            val message: String,
            val throwable: Throwable?,
            val metadata: Map<String, Any?>?
        )

        override fun isEnabled(level: LogLevel): Boolean = level.priority >= minLevel.priority && level != LogLevel.NONE

        override fun log(
            level: LogLevel,
            tag: String,
            message: String,
            throwable: Throwable?,
            metadata: Map<String, Any?>?
        ) {
            loggedEntries.add(LoggedItem(level, tag, message, throwable, metadata))
        }

        override fun flush() {
            flushCalled = true
        }
    }

    @Test
    fun `engine dispatches log to enabled sinks`() {
        val sink = TestSink()
        val configProvider = DefaultLoggerConfigProvider()
        val engine = LoggerEngine(listOf(sink), configProvider)

        engine.log(
            level = LogLevel.INFO,
            explicitTag = "TestTag",
            messageProducer = { "Test message" },
            throwable = null,
            metadata = null
        )

        assertEquals(1, sink.loggedEntries.size)
        val entry = sink.loggedEntries.first()
        assertEquals(LogLevel.INFO, entry.level)
        assertEquals("TestTag", entry.tag)
        assertEquals("Test message", entry.message)
    }

    @Test
    fun `engine drops log when no sink is enabled for level`() {
        val sink = TestSink(minLevel = LogLevel.WARN)
        val configProvider = DefaultLoggerConfigProvider()
        val engine = LoggerEngine(listOf(sink), configProvider)

        var producerInvoked = false
        engine.log(
            level = LogLevel.DEBUG,
            explicitTag = "TestTag",
            messageProducer = {
                producerInvoked = true
                "Should not be evaluated"
            },
            throwable = null,
            metadata = null
        )

        assertEquals(0, sink.loggedEntries.size)
        assertFalse(producerInvoked) // Lazy evaluation verified!
    }

    @Test
    fun `engine sanitizes message when sanitization is enabled`() {
        val sink = TestSink()
        val configProvider = DefaultLoggerConfigProvider(LoggerConfig(isSanitizationEnabled = true))
        val engine = LoggerEngine(listOf(sink), configProvider)

        engine.log(
            level = LogLevel.DEBUG,
            explicitTag = "AuthTag",
            messageProducer = { "User token: Bearer eyJ123.secret" },
            throwable = null,
            metadata = null
        )

        assertEquals(1, sink.loggedEntries.size)
        val entry = sink.loggedEntries.first()
        assertTrue(entry.message.contains("Bearer [MASKED_TOKEN]"))
        assertFalse(entry.message.contains("eyJ123.secret"))
    }

    @Test
    fun `engine preserves raw message when sanitization is disabled`() {
        val sink = TestSink()
        val configProvider = DefaultLoggerConfigProvider(LoggerConfig(isSanitizationEnabled = false))
        val engine = LoggerEngine(listOf(sink), configProvider)

        engine.log(
            level = LogLevel.DEBUG,
            explicitTag = "AuthTag",
            messageProducer = { "User token: Bearer eyJ123.secret" },
            throwable = null,
            metadata = null
        )

        assertEquals(1, sink.loggedEntries.size)
        val entry = sink.loggedEntries.first()
        assertEquals("User token: Bearer eyJ123.secret", entry.message)
    }

    @Test
    fun `failing sink does not crash engine or other sinks`() {
        val failingSink = object : LogSink {
            override fun isEnabled(level: LogLevel): Boolean = true
            override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any?>?) {
                throw RuntimeException("Sink disk failure simulation")
            }
        }
        val healthySink = TestSink()
        val configProvider = DefaultLoggerConfigProvider()
        val engine = LoggerEngine(listOf(failingSink, healthySink), configProvider)

        engine.log(
            level = LogLevel.ERROR,
            explicitTag = "CrashTest",
            messageProducer = { "Critical operation" },
            throwable = null,
            metadata = null
        )

        assertEquals(1, healthySink.loggedEntries.size)
    }

    @Test
    fun `flush calls flush on all sinks`() {
        val sink1 = TestSink()
        val sink2 = TestSink()
        val configProvider = DefaultLoggerConfigProvider()
        val engine = LoggerEngine(listOf(sink1, sink2), configProvider)

        engine.flush()

        assertTrue(sink1.flushCalled)
        assertTrue(sink2.flushCalled)
    }
}

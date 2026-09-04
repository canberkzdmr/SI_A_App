package com.cbo.core.logger

import com.cbo.core.logger.api.DefaultLoggerConfigProvider
import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink
import com.cbo.core.logger.api.LoggerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLoggerTest {

    private class MockSink : LogSink {
        val messages = mutableListOf<String>()
        val levels = mutableListOf<LogLevel>()
        val tags = mutableListOf<String>()
        var flushed = false

        override fun isEnabled(level: LogLevel): Boolean = true

        override fun log(
            level: LogLevel,
            tag: String,
            message: String,
            throwable: Throwable?,
            metadata: Map<String, Any?>?
        ) {
            levels.add(level)
            tags.add(tag)
            messages.add(message)
        }

        override fun flush() {
            flushed = true
        }
    }

    private lateinit var mockSink: MockSink
    private lateinit var configProvider: DefaultLoggerConfigProvider

    @Before
    fun setUp() {
        mockSink = MockSink()
        configProvider = DefaultLoggerConfigProvider()

        // Inject engine for testing
        val engine = com.cbo.core.logger.engine.LoggerEngine(listOf(mockSink), configProvider)
        val engineField = AppLogger::class.java.getDeclaredField("engine")
        engineField.isAccessible = true
        engineField.set(AppLogger, engine)

        val configField = AppLogger::class.java.getDeclaredField("configProvider")
        configField.isAccessible = true
        configField.set(AppLogger, configProvider)
    }

    @Test
    fun `AppLogger logs all standard levels with lambda`() {
        AppLogger.v(tag = "VTag") { "Verbose log" }
        AppLogger.d(tag = "DTag") { "Debug log" }
        AppLogger.i(tag = "ITag") { "Info log" }
        AppLogger.w(tag = "WTag") { "Warn log" }
        AppLogger.e(tag = "ETag") { "Error log" }

        assertEquals(5, mockSink.messages.size)
        assertEquals(listOf(LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR), mockSink.levels)
        assertEquals(listOf("VTag", "DTag", "ITag", "WTag", "ETag"), mockSink.tags)
    }

    @Test
    fun `AppLogger logs string overloads correctly`() {
        AppLogger.v("Direct verbose", tag = "TagV")
        AppLogger.d("Direct debug", tag = "TagD")
        AppLogger.i("Direct info", tag = "TagI")
        AppLogger.w("Direct warn", tag = "TagW")
        AppLogger.e("Direct error", tag = "TagE")

        assertEquals(5, mockSink.messages.size)
        assertEquals(listOf("Direct verbose", "Direct debug", "Direct info", "Direct warn", "Direct error"), mockSink.messages)
    }

    @Test
    fun `AppLogger tagged builder prefixes tag correctly`() {
        val tagged = AppLogger.tag("MyCustomTag")
        tagged.v { "V custom" }
        tagged.d { "D custom" }
        tagged.i { "I custom" }
        tagged.w { "W custom" }
        tagged.e { "E custom" }
        tagged.d("D custom string")
        tagged.e("E custom string")

        assertEquals(7, mockSink.tags.size)
        assertTrue(mockSink.tags.all { it == "MyCustomTag" })
    }

    @Test
    fun `AppLogger error overload with throwable logs message and exception`() {
        val exception = IllegalArgumentException("Invalid state")
        AppLogger.e(exception, tag = "ExTag", message = "Custom failure")

        assertEquals(1, mockSink.messages.size)
        assertEquals("Custom failure", mockSink.messages.first())
        assertEquals("ExTag", mockSink.tags.first())
    }

    @Test
    fun `AppLogger updateConfig updates runtime config`() {
        AppLogger.updateConfig(LoggerConfig(isSanitizationEnabled = false, minDbLogLevel = LogLevel.ERROR))

        assertEquals(LogLevel.ERROR, configProvider.minDbLogLevel.value)
    }

    @Test
    fun `AppLogger flush invokes engine flush`() {
        AppLogger.flush()

        assertTrue(mockSink.flushed)
    }
}

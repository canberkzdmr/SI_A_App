package com.cbo.core.logger

import com.cbo.core.logger.api.DefaultLoggerConfigProvider
import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LoggerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggerConfigTest {

    @Test
    fun `default config has expected baseline values`() {
        val provider = DefaultLoggerConfigProvider()

        assertTrue(provider.isDbLoggingEnabled.value)
        assertEquals(LogLevel.DEBUG, provider.minDbLogLevel.value)
        assertEquals(7, provider.retentionDays.value)
        assertEquals(50, provider.maxDbSizeMb.value)
        assertEquals(2000L, provider.batchIntervalMs.value)
        assertEquals(50, provider.batchSize.value)
        assertTrue(provider.isSanitizationEnabled.value)
        assertTrue(provider.enableShortcutOnDebug.value)
    }

    @Test
    fun `updateConfig updates state flows dynamically`() {
        val provider = DefaultLoggerConfigProvider()

        val updated = LoggerConfig(
            isDbLoggingEnabled = false,
            minDbLogLevel = LogLevel.WARN,
            retentionDays = 14,
            maxDbSizeMb = 100,
            batchIntervalMs = 5000L,
            batchSize = 20,
            isSanitizationEnabled = false,
            enableShortcutOnDebug = false
        )

        provider.updateConfig(updated)

        assertFalse(provider.isDbLoggingEnabled.value)
        assertEquals(LogLevel.WARN, provider.minDbLogLevel.value)
        assertEquals(14, provider.retentionDays.value)
        assertEquals(100, provider.maxDbSizeMb.value)
        assertEquals(5000L, provider.batchIntervalMs.value)
        assertEquals(20, provider.batchSize.value)
        assertFalse(provider.isSanitizationEnabled.value)
        assertFalse(provider.enableShortcutOnDebug.value)
    }
}

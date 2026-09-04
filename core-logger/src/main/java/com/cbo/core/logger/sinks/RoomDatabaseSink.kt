package com.cbo.core.logger.sinks

import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink
import com.cbo.core.logger.api.LoggerConfigProvider
import com.cbo.core.logger.database.LogEntity
import com.cbo.core.logger.engine.LogBufferChannel
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Dispatches log messages to the isolated Room Database via LogBufferChannel.
 */
class RoomDatabaseSink(
    private val bufferChannel: LogBufferChannel,
    private val configProvider: LoggerConfigProvider
) : LogSink {

    override fun isEnabled(level: LogLevel): Boolean {
        if (!configProvider.isDbLoggingEnabled.value) return false
        val minLevel = configProvider.minDbLogLevel.value
        return level.priority >= minLevel.priority && level != LogLevel.NONE
    }

    override fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
        metadata: Map<String, Any?>?
    ) {
        if (!isEnabled(level)) return

        val stackTraceString = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }

        val metadataString = metadata?.entries?.joinToString(
            prefix = "{",
            postfix = "}"
        ) { "\"${it.key}\":\"${it.value}\"" }

        val entity = LogEntity(
            timestamp = System.currentTimeMillis(),
            level = level.name,
            tag = tag,
            message = message,
            throwable = stackTraceString,
            threadName = Thread.currentThread().name,
            metadata = metadataString
        )

        bufferChannel.enqueue(entity)
    }

    override fun flush() {
        bufferChannel.flush()
    }
}

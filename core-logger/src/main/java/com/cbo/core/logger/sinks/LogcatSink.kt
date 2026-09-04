package com.cbo.core.logger.sinks

import android.util.Log
import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink

/**
 * Dispatches log messages to Android Logcat.
 * Automatically splits messages exceeding Android's 4000-char limit into clean chunks.
 */
class LogcatSink(
    private val minLevel: LogLevel = LogLevel.VERBOSE
) : LogSink {

    companion object {
        private const val MAX_LOG_LENGTH = 4000
    }

    override fun isEnabled(level: LogLevel): Boolean {
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

        val formattedMessage = if (metadata.isNullOrEmpty()) {
            message
        } else {
            "$message | meta=$metadata"
        }

        if (formattedMessage.length <= MAX_LOG_LENGTH) {
            printLog(level, tag, formattedMessage, throwable)
            return
        }

        // Chunk long messages
        var i = 0
        val length = formattedMessage.length
        while (i < length) {
            var newline = formattedMessage.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = formattedMessage.substring(i, end)
                printLog(level, tag, part, null)
                i = end
            } while (i < newline)
            i++
        }

        if (throwable != null) {
            printLog(level, tag, "Exception stack trace:", throwable)
        }
    }

    private fun printLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> if (throwable != null) Log.v(tag, message, throwable) else Log.v(tag, message)
            LogLevel.DEBUG -> if (throwable != null) Log.d(tag, message, throwable) else Log.d(tag, message)
            LogLevel.INFO -> if (throwable != null) Log.i(tag, message, throwable) else Log.i(tag, message)
            LogLevel.WARN -> if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
            LogLevel.ERROR -> if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
            LogLevel.NONE -> {}
        }
    }
}

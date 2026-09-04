package com.cbo.core.logger.engine

import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink
import com.cbo.core.logger.api.LoggerConfigProvider
import com.cbo.core.logger.filter.LogSanitizer

/**
 * Core logging engine coordinating filtering, sanitization, auto-tagging, and sink dispatching.
 */
class LoggerEngine(
    private val sinks: List<LogSink>,
    private val configProvider: LoggerConfigProvider
) {

    fun log(
        level: LogLevel,
        explicitTag: String?,
        messageProducer: () -> String,
        throwable: Throwable?,
        metadata: Map<String, Any?>?
    ) {
        // Fast-path: Check if ANY sink accepts this level before evaluating expensive message or stack trace
        val activeSinks = sinks.filter { it.isEnabled(level) }
        if (activeSinks.isEmpty()) return

        val tag = explicitTag ?: StackTraceHelper.resolveCallerTag()
        val rawMessage = messageProducer()

        val sanitizedMessage = if (configProvider.isSanitizationEnabled.value) {
            LogSanitizer.sanitize(rawMessage)
        } else {
            rawMessage
        }

        for (sink in activeSinks) {
            try {
                sink.log(
                    level = level,
                    tag = tag,
                    message = sanitizedMessage,
                    throwable = throwable,
                    metadata = metadata
                )
            } catch (e: Exception) {
                // Log sinks must never crash the app
                try {
                    android.util.Log.e("LoggerEngine", "Sink dispatch failed: ${sink.javaClass.simpleName}", e)
                } catch (_: Throwable) {
                    // Ignored in headless unit test environment
                }
            }
        }
    }

    fun flush() {
        for (sink in sinks) {
            try {
                sink.flush()
            } catch (e: Exception) {
                android.util.Log.e("LoggerEngine", "Sink flush failed", e)
            }
        }
    }
}

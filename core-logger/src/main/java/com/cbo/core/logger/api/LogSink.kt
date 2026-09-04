package com.cbo.core.logger.api

/**
 * Interface representing a destination where logs are dispatched.
 * Follows the Observer / Composite pattern (e.g. LogcatSink, RoomDatabaseSink).
 */
interface LogSink {
    /**
     * Whether this sink is currently active and accepting logs at the given level.
     */
    fun isEnabled(level: LogLevel): Boolean

    /**
     * Process and output a log entry.
     */
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null
    )

    /**
     * Optional method to flush any buffered logs immediately.
     */
    fun flush() {}
}

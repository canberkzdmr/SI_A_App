package com.cbo.core.logger.api

enum class LogLevel(val priority: Int, val shortName: String) {
    VERBOSE(2, "V"),
    DEBUG(3, "D"),
    INFO(4, "I"),
    WARN(5, "W"),
    ERROR(6, "E"),
    NONE(7, "-");

    companion object {
        fun fromString(value: String, default: LogLevel = DEBUG): LogLevel {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: default
        }
    }
}

package com.cbo.core.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DatePattern {
    const val FULL = "dd-MM-yyyy HH:mm:ss"
    const val DATE_ONLY = "dd-MM-yyyy"
    const val TIME_ONLY = "HH:mm:ss"
    const val READABLE = "dd MMM yyyy"
    const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}

object DateUtil {
    /**
     * Returns the current date and time formatted using the FULL pattern.
     * Format: "dd-MM-yyyy HH:mm:ss" (e.g., "04-09-2025 11:18:00")
     * Uses [DatePattern.FULL] for full timestamp representation.
     */
    fun fullDate(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.FULL)
        return LocalDateTime.now().format(formatter)
    }

    /**
     * Returns the current date formatted using the DATE_ONLY pattern.
     * Format: "dd-MM-yyyy" (e.g., "04-09-2025")
     * Uses [DatePattern.DATE_ONLY] for date-only representation.
     */
    fun dateOnly(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.DATE_ONLY)
        return LocalDateTime.now().format(formatter)
    }

    /**
     * Returns the current time formatted using the TIME_ONLY pattern.
     * Format: "HH:mm:ss" (e.g., "11:18:00")
     * Uses [DatePattern.TIME_ONLY] for time-only representation.
     */
    fun timeOnly(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.TIME_ONLY)
        return LocalDateTime.now().format(formatter)
    }

    /**
     * Returns the current date formatted in a human-readable style.
     * Format: "dd MMM yyyy" (e.g., "04 Sep 2025")
     * Uses [DatePattern.READABLE] for user-friendly display.
     */
    fun readable(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.READABLE)
        return LocalDateTime.now().format(formatter)
    }

    /**
     * Returns the current date and time formatted in ISO 8601 standard.
     * Format: "yyyy-MM-dd'T'HH:mm:ss'Z'" (e.g., "2025-09-04T11:18:00Z")
     * Uses [DatePattern.ISO_8601] for standardized machine-readable output.
     */
    fun iso8601(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.ISO_8601)
        return LocalDateTime.now().format(formatter)
    }
}

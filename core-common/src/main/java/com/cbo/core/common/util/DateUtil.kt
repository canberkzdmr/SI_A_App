package com.cbo.core.common.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatePattern {
    const val FULL = "dd-MM-yyyy HH:mm:ss"
    const val DATE_ONLY = "dd-MM-yyyy"
    const val TIME_ONLY = "HH:mm:ss"
    const val READABLE = "dd MMM yyyy"
    const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    // Additional patterns for date picker
    const val SHORT_DATE = "MMM dd, yyyy"      // Jan 15, 2024
    const val MEDIUM_DATE = "dd MMM yyyy"      // 15 Jan 2024
    const val LONG_DATE = "EEEE, MMMM dd, yyyy"  // Monday, January 15, 2024
}

object DateUtil {
    // ... existing functions ...

    /**
     * Formats epoch milliseconds to a string using the specified pattern.
     * @param millis Epoch milliseconds (e.g., from DatePicker)
     * @param pattern Date format pattern (use DatePattern constants)
     * @return Formatted date string
     */
    fun format(millis: Long, pattern: String = DatePattern.SHORT_DATE): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(millis))
    }

    /**
     * Formats epoch milliseconds to readable format.
     * Format: "dd MMM yyyy" (e.g., "15 Jan 2024")
     */
    fun formatReadable(millis: Long): String {
        return format(millis, DatePattern.READABLE)
    }

    /**
     * Formats epoch milliseconds to short date format.
     * Format: "MMM dd, yyyy" (e.g., "Jan 15, 2024")
     */
    fun formatShortDate(millis: Long): String {
        return format(millis, DatePattern.SHORT_DATE)
    }

    /**
     * Formats epoch milliseconds to date only format.
     * Format: "dd-MM-yyyy" (e.g., "15-01-2024")
     */
    fun formatDateOnly(millis: Long): String {
        return format(millis, DatePattern.DATE_ONLY)
    }

    /**
     * Converts epoch milliseconds to LocalDateTime
     */
    fun toLocalDateTime(millis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        )
    }

    /**
     * Converts LocalDateTime to epoch milliseconds
     */
    fun toMillis(localDateTime: LocalDateTime): Long {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    // Existing functions remain the same
    fun fullDate(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.FULL)
        return LocalDateTime.now().format(formatter)
    }

    fun dateOnly(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.DATE_ONLY)
        return LocalDateTime.now().format(formatter)
    }

    fun timeOnly(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.TIME_ONLY)
        return LocalDateTime.now().format(formatter)
    }

    fun readable(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.READABLE)
        return LocalDateTime.now().format(formatter)
    }

    fun iso8601(): String {
        val formatter = DateTimeFormatter.ofPattern(DatePattern.ISO_8601)
        return LocalDateTime.now().format(formatter)
    }
}

/**
 * Extension function to convert Long to Calendar
 */
fun Long.toCalendar(): Calendar = Calendar.getInstance().apply {
    timeInMillis = this@toCalendar
}
package com.cbo.core.common.util

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * Executes a synchronous block of code wrapped in a Firebase Performance [Trace].
 *
 * Catches any initialization or runtime issues so performance tracking
 * never crashes business logic or unit tests.
 */
inline fun <T> traceMetric(
    traceName: String,
    crossinline block: (Trace?) -> T
): T {
    val trace: Trace? = try {
        FirebasePerformance.getInstance().newTrace(traceName).apply { start() }
    } catch (_: Throwable) {
        null
    }

    return try {
        block(trace)
    } finally {
        try {
            trace?.stop()
        } catch (_: Throwable) {}
    }
}

/**
 * Executes a suspend block of code wrapped in a Firebase Performance [Trace].
 *
 * Catches any initialization or runtime issues so performance tracking
 * never crashes business logic or unit tests.
 */
suspend inline fun <T> traceMetricSuspend(
    traceName: String,
    crossinline block: suspend (Trace?) -> T
): T {
    val trace: Trace? = try {
        FirebasePerformance.getInstance().newTrace(traceName).apply { start() }
    } catch (_: Throwable) {
        null
    }

    return try {
        block(trace)
    } finally {
        try {
            trace?.stop()
        } catch (_: Throwable) {}
    }
}

/**
 * Safely adds or updates a metric value on this [Trace] without throwing exceptions.
 */
fun Trace?.safePutMetric(metricName: String, value: Long) {
    if (this == null) return
    try {
        putMetric(metricName, value)
    } catch (_: Throwable) {}
}

/**
 * Safely increments a metric value on this [Trace] without throwing exceptions.
 */
fun Trace?.safeIncrementMetric(metricName: String, incrementBy: Long = 1L) {
    if (this == null) return
    try {
        incrementMetric(metricName, incrementBy)
    } catch (_: Throwable) {}
}

/**
 * Safely sets an attribute key-value on this [Trace] without throwing exceptions.
 */
fun Trace?.safePutAttribute(attributeName: String, value: String) {
    if (this == null) return
    try {
        putAttribute(attributeName, value)
    } catch (_: Throwable) {}
}

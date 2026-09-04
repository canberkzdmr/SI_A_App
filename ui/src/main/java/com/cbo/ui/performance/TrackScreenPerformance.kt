package com.cbo.ui.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * A Composable that tracks the time spent on a screen using a Firebase Performance [Trace].
 *
 * When the screen enters composition, a trace named `screen_<screenName>` starts.
 * When the screen leaves composition (or [screenName] changes), the trace stops.
 */
@Composable
fun TrackScreenPerformance(
    screenName: String,
    attributes: Map<String, String> = emptyMap()
) {
    DisposableEffect(screenName) {
        val traceName = "screen_$screenName"
        val trace: Trace? = try {
            FirebasePerformance.getInstance().newTrace(traceName).apply {
                attributes.forEach { (key, value) ->
                    try {
                        putAttribute(key, value)
                    } catch (_: Throwable) {}
                }
                start()
            }
        } catch (_: Throwable) {
            null
        }

        onDispose {
            try {
                trace?.stop()
            } catch (_: Throwable) {}
        }
    }
}

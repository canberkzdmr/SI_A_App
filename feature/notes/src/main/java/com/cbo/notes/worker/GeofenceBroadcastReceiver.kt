package com.cbo.notes.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cbo.core.common.constants.FeatureFlagManager
import com.cbo.core.logger.AppLogger
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!FeatureFlagManager.ENABLE_BACKGROUND_LOCATION) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            AppLogger.e("GeofencingEvent error: ${geofencingEvent?.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach { geofence ->
                val noteId = geofence.requestId.toIntOrNull()
                if (noteId != null) {
                    val prefs = context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
                    val lastTrigger = prefs.getLong("last_trigger_$noteId", 0L)
                    val now = System.currentTimeMillis()
                    
                    // 10 dakika (600,000 ms) debounce süresi ile jitter önleniyor.
                    if (now - lastTrigger > 10 * 60 * 1000) {
                        prefs.edit().putLong("last_trigger_$noteId", now).apply()
                        AppLogger.d("Entered geofence for note: $noteId")
                        triggerNotificationWorker(context, noteId)
                    } else {
                        AppLogger.d("Geofence debounce active for note: $noteId, ignoring trigger.")
                    }
                }
            }
        }
    }

    private fun triggerNotificationWorker(context: Context, noteId: Int) {
        val scheduler = ReminderScheduler(context)
        // Schedule immediately (0 delay) and pass LOCATION triggerType
        scheduler.scheduleReminder(noteId, "Konum Hatırlatıcısı!", System.currentTimeMillis() + 1000, triggerType = "LOCATION")
    }
}

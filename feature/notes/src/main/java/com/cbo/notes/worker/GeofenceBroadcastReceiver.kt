package com.cbo.notes.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cbo.core.common.constants.FeatureFlagManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!FeatureFlagManager.ENABLE_BACKGROUND_LOCATION) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent error: ${geofencingEvent?.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach { geofence ->
                val noteId = geofence.requestId.toIntOrNull()
                if (noteId != null) {
                    Log.d(TAG, "Entered geofence for note: $noteId")
                    // Note: Ideally, we should fetch the note title from DB here.
                    // For now, we will trigger a notification with a generic message or enqueue a Worker
                    // to fetch the details and show notification. Let's trigger a one-time worker immediately.
                    triggerNotificationWorker(context, noteId)
                }
            }
        }
    }

    private fun triggerNotificationWorker(context: Context, noteId: Int) {
        val scheduler = ReminderScheduler(context)
        // Schedule immediately (0 delay) and pass LOCATION triggerType
        scheduler.scheduleReminder(noteId, "Konum Hatırlatıcısı!", System.currentTimeMillis() + 1000, triggerType = "LOCATION")
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}

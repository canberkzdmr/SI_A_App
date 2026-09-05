package com.cbo.notes.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cbo.core.common.constants.FeatureFlagManager
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.logger.AppLogger
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var noteDao: NoteDao

    @Inject
    lateinit var notificationHelper: ReminderNotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (!FeatureFlagManager.ENABLE_BACKGROUND_LOCATION) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            AppLogger.e("GeofencingEvent error: ${geofencingEvent?.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            if (triggeringGeofences.isNullOrEmpty()) return

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
                    val now = System.currentTimeMillis()

                    for (geofence in triggeringGeofences) {
                        val noteId = geofence.requestId.toIntOrNull() ?: continue
                        val lastTrigger = prefs.getLong("last_trigger_$noteId", 0L)

                        // 10 dakika (600,000 ms) debounce süresi ile jitter önleniyor.
                        if (now - lastTrigger > 10 * 60 * 1000) {
                            prefs.edit().putLong("last_trigger_$noteId", now).apply()
                            AppLogger.d("Entered geofence for note: $noteId, displaying notification immediately.")

                            val note = noteDao.getNoteById(noteId)
                            if (note != null && !note.isDeleted && note.isLocationReminderEnabled) {
                                notificationHelper.showNotification(
                                    note = note,
                                    title = note.title,
                                    triggerType = "LOCATION"
                                )
                            } else {
                                AppLogger.d("Note $noteId was deleted or location reminder disabled, skipping notification.")
                            }
                        } else {
                            AppLogger.d("Geofence debounce active for note: $noteId, ignoring trigger.")
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e("Error processing geofence trigger in GeofenceBroadcastReceiver", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}


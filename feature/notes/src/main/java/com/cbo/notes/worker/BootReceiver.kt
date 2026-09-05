package com.cbo.notes.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.logger.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var noteDao: NoteDao

    @Inject
    lateinit var locationReminderManager: LocationReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            AppLogger.d("Device rebooted or package replaced. Rescheduling reminders.")
            
            val reminderScheduler = ReminderScheduler(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Reschedule Time Reminders
                    val currentTime = System.currentTimeMillis()
                    // 1 year from now roughly
                    val activeTimeReminders = noteDao.getNotesWithRemindersBetween(currentTime, currentTime + 31536000000L) 
                    activeTimeReminders.forEach { note ->
                        val reminderTime = note.reminderTime
                        if (reminderTime != null && reminderTime > currentTime) {
                            reminderScheduler.scheduleReminder(note.id, note.title, reminderTime)
                            AppLogger.d("Rescheduled time reminder for note ${note.id}")
                        }
                    }

                    // 2. Reschedule Location Reminders (Geofences)
                    val activeLocationReminders = noteDao.getNotesWithActiveLocationReminders()
                    activeLocationReminders.forEach { note ->
                        val lat = note.reminderLatitude
                        val lng = note.reminderLongitude
                        if (note.isLocationReminderEnabled && lat != null && lng != null) {
                            val radius = note.reminderRadius ?: LocationReminderManager.DEFAULT_RADIUS
                            locationReminderManager.addLocationReminder(
                                noteId = note.id,
                                latitude = lat,
                                longitude = lng,
                                radiusInMeters = radius
                            )
                            AppLogger.d("Rescheduled location reminder for note ${note.id}")
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e("Error rescheduling reminders on boot", e)
                }
            }
        }
    }
}

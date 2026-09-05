package com.cbo.notes.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.logger.AppLogger
import com.cbo.notes.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager Worker that triggers a notification for a note reminder.
 * 
 * This worker is scheduled to run at the exact reminder time and displays
 * a notification to remind the user about the note.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val noteDao: NoteDao,
    private val notificationHelper: ReminderNotificationHelper
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val noteId = inputData.getInt(KEY_NOTE_ID, -1)
        val noteTitle = inputData.getString(KEY_NOTE_TITLE) ?: "Note Reminder"
        
        AppLogger.d("Reminder triggered for note: $noteId - $noteTitle")
        
        if (noteId == -1) {
            AppLogger.e("Invalid note ID")
            return Result.failure()
        }
        
        return try {
            // Verify the note still exists
            val note = noteDao.getNoteById(noteId)
            if (note == null || note.isDeleted) {
                AppLogger.d("Note was deleted or reminder was removed, skipping notification")
                return Result.success()
            }
            
            // Use the title from the database to ensure it's up to date
            val currentTitle = note.title
            
            val triggerType = inputData.getString("trigger_type") ?: "TIME"
            
            // Show the notification
            notificationHelper.showNotification(note, currentTitle, triggerType)
            
            // Handle repeat or clear only for time-based triggers
            if (triggerType == "TIME") {
                if (note.reminderRepeat != null && note.reminderRepeat != "NONE") {
                    scheduleNextReminder(note, currentTitle)
                } else {
                    noteDao.updateReminder(noteId, null)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            AppLogger.e("Error showing reminder notification", e)
            Result.failure()
        }
    }
    
    private suspend fun scheduleNextReminder(note: com.cbo.core.database.entity.NoteEntity, title: String) {
        val nextTime = calculateNextReminderTime(note.reminderTime ?: System.currentTimeMillis(), note.reminderRepeat)
        noteDao.updateReminder(note.id, nextTime, note.reminderRepeat, note.reminderPriority)
        
        val delay = nextTime - System.currentTimeMillis()
        if (delay > 0) {
            val inputData = androidx.work.Data.Builder()
                .putInt(KEY_NOTE_ID, note.id)
                .putString(KEY_NOTE_TITLE, title)
                .build()
            
            val reminderRequest = androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("note_reminder")
                .addTag("note_reminder_${note.id}")
                .build()
                
            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                getWorkName(note.id),
                androidx.work.ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        }
    }

    private fun calculateNextReminderTime(currentTime: Long, repeat: String?): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTime
        when (repeat) {
            "DAILY" -> calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(java.util.Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(java.util.Calendar.YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val CHANNEL_ID_HIGH = "note_reminders_high"
        const val CHANNEL_ID_DEFAULT = "note_reminders_default"
        const val KEY_NOTE_ID = "note_id"
        const val KEY_NOTE_TITLE = "note_title"
        const val EXTRA_NOTE_ID = "extra_note_id"
        
        /** Unique work name prefix for reminder workers */
        const val WORK_NAME_PREFIX = "reminder_"
        
        fun getWorkName(noteId: Int) = "$WORK_NAME_PREFIX$noteId"
    }
}


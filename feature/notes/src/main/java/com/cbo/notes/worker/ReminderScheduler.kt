package com.cbo.notes.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cbo.core.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler responsible for managing reminder work requests.
 * 
 * Uses WorkManager to schedule one-time work requests that will trigger
 * at the specified reminder time to show a notification.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Schedules a reminder notification for a note at the specified time.
     * 
     * @param noteId The ID of the note to remind about
     * @param noteTitle The title of the note (shown in notification)
     * @param reminderTime The timestamp (in milliseconds) when the reminder should trigger
     */
    fun scheduleReminder(noteId: Int, noteTitle: String, reminderTime: Long, triggerType: String = "TIME") {
        val currentTime = System.currentTimeMillis()
        val delay = reminderTime - currentTime
        
        if (delay <= 0) {
            AppLogger.w("Reminder time is in the past, not scheduling for note: $noteId")
            return
        }
        
        AppLogger.d("Scheduling reminder for note $noteId in ${delay / 1000} seconds")
        
        val inputData = Data.Builder()
            .putInt(ReminderWorker.KEY_NOTE_ID, noteId)
            .putString(ReminderWorker.KEY_NOTE_TITLE, noteTitle)
            .putString("trigger_type", triggerType)
            .build()
        
        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG)
            .addTag("$WORK_TAG_NOTE_PREFIX$noteId")
            .build()
        
        // Use REPLACE policy so updating a reminder cancels the previous one
        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderWorker.getWorkName(noteId),
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
        
        AppLogger.d("Reminder scheduled successfully for note: $noteId at $reminderTime")
    }
    
    /**
     * Cancels a scheduled reminder for a note.
     * 
     * @param noteId The ID of the note whose reminder should be cancelled
     */
    fun cancelReminder(noteId: Int) {
        AppLogger.d("Cancelling reminder for note: $noteId")
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.getWorkName(noteId))
    }
    
    /**
     * Cancels all scheduled reminders.
     */
    fun cancelAllReminders() {
        AppLogger.d("Cancelling all reminders")
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
    
    /**
     * Updates a reminder by cancelling the existing one and scheduling a new one.
     * 
     * @param noteId The ID of the note
     * @param noteTitle The title of the note
     * @param newReminderTime The new reminder time
     */
    fun updateReminder(noteId: Int, noteTitle: String, newReminderTime: Long) {
        // Cancel existing and schedule new (REPLACE policy handles this)
        scheduleReminder(noteId, noteTitle, newReminderTime)
    }

    companion object {
        private const val WORK_TAG = "note_reminder"
        private const val WORK_TAG_NOTE_PREFIX = "note_reminder_"
    }
}



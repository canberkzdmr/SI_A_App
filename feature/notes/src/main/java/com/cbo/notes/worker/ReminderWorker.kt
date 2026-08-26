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
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.entity.NoteEntity
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
    private val noteDao: NoteDao
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val noteId = inputData.getInt(KEY_NOTE_ID, -1)
        val noteTitle = inputData.getString(KEY_NOTE_TITLE) ?: "Note Reminder"
        
        Log.d(TAG, "Reminder triggered for note: $noteId - $noteTitle")
        
        if (noteId == -1) {
            Log.e(TAG, "Invalid note ID")
            return Result.failure()
        }
        
        return try {
            // Verify the note still exists
            val note = noteDao.getNoteById(noteId)
            if (note == null || note.isDeleted) {
                Log.d(TAG, "Note was deleted or reminder was removed, skipping notification")
                return Result.success()
            }
            
            // Use the title from the database to ensure it's up to date
            val currentTitle = note.title
            
            val triggerType = inputData.getString("trigger_type") ?: "TIME"
            
            // Show the notification
            showNotification(note, currentTitle, triggerType)
            
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
            Log.e(TAG, "Error showing reminder notification", e)
            Result.failure()
        }
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(note: NoteEntity, title: String, triggerType: String) {
        val priorityLevel = note.reminderPriority ?: "DEFAULT"
        val channelId = if (priorityLevel == "HIGH") CHANNEL_ID_HIGH else CHANNEL_ID_DEFAULT
        createNotificationChannels()
        
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted")
                return
            }
        }
        
        // Convert Markdown content to plain text for notification display
        val plainTextContent = stripMarkdown(note.content)
        
        // Create intent to open the note when notification is tapped
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTE_ID, note.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            note.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Snooze actions
        val snooze15Intent = Intent(context, SnoozeReceiver::class.java).apply {
            action = SnoozeReceiver.ACTION_SNOOZE
            putExtra(EXTRA_NOTE_ID, note.id)
            putExtra(SnoozeReceiver.EXTRA_SNOOZE_MINUTES, 15)
            putExtra(KEY_NOTE_TITLE, title)
        }
        val snooze15Pending = PendingIntent.getBroadcast(
            context, note.id * 10 + 1, snooze15Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snooze60Intent = Intent(context, SnoozeReceiver::class.java).apply {
            action = SnoozeReceiver.ACTION_SNOOZE
            putExtra(EXTRA_NOTE_ID, note.id)
            putExtra(SnoozeReceiver.EXTRA_SNOOZE_MINUTES, 60)
            putExtra(KEY_NOTE_TITLE, title)
        }
        val snooze60Pending = PendingIntent.getBroadcast(
            context, note.id * 10 + 2, snooze60Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build and show the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(if (triggerType == "LOCATION") "📍 $title" else title)
            .setContentText(plainTextContent.take(100).let { if (plainTextContent.length > 100) "$it..." else it })
            .setStyle(NotificationCompat.BigTextStyle().bigText(plainTextContent.take(300)))
            .setPriority(if (priorityLevel == "HIGH") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        if (triggerType == "LOCATION") {
            val turnOffIntent = Intent(context, SnoozeReceiver::class.java).apply {
                action = SnoozeReceiver.ACTION_TURN_OFF_LOCATION_REMINDER
                putExtra(EXTRA_NOTE_ID, note.id)
            }
            val turnOffPending = PendingIntent.getBroadcast(
                context, note.id * 10 + 3, turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Bildirimini iptal et", turnOffPending)
        } else {
            builder.addAction(0, "15 Dk Ertele", snooze15Pending)
            builder.addAction(0, "1 Saat Ertele", snooze60Pending)
        }
            
        if (priorityLevel == "LOW") {
            builder.setSilent(true)
        }
        
        NotificationManagerCompat.from(context).notify(note.id, builder.build())
        Log.d(TAG, "Notification shown for note: ${note.id} with priority $priorityLevel")
    }
    
    /**
     * Strips Markdown syntax from content and returns plain text.
     */
    private fun stripMarkdown(markdown: String): String {
        if (markdown.isBlank()) return ""
        
        // Simple regex to strip basic markdown characters for notification
        return markdown
            .replace(Regex("[#*`_\\[\\]()]"), "") // Remove common markdown symbols
            .replace(Regex("!.*\\]\\(.*\\)"), "") // Remove images
            .replace(Regex("<.*?>"), "") // Remove any remaining HTML if present
            .trim()
            .replace(Regex("\\s+"), " ") // Replace multiple whitespaces with single space
    }
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // High priority channel
            val highChannel = NotificationChannel(CHANNEL_ID_HIGH, "Yüksek Öncelikli Hatırlatıcılar", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Yüksek öncelikli not hatırlatıcıları"
                enableVibration(true)
                enableLights(true)
            }
            
            // Default priority channel
            val defaultChannel = NotificationChannel(CHANNEL_ID_DEFAULT, "Normal Hatırlatıcılar", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Normal öncelikli not hatırlatıcıları"
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager.createNotificationChannel(highChannel)
            notificationManager.createNotificationChannel(defaultChannel)
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
        private const val TAG = "ReminderWorker"
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


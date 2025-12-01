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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cbo.core.database.dao.NoteDao
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

    override suspend fun doWork(): Result {
        val noteId = inputData.getInt(KEY_NOTE_ID, -1)
        val noteTitle = inputData.getString(KEY_NOTE_TITLE) ?: "Note Reminder"
        
        Log.d(TAG, "Reminder triggered for note: $noteId - $noteTitle")
        
        if (noteId == -1) {
            Log.e(TAG, "Invalid note ID")
            return Result.failure()
        }
        
        return try {
            // Verify the note still exists and has the reminder set
            val note = noteDao.getNoteWithReminder(noteId)
            if (note == null || note.isDeleted) {
                Log.d(TAG, "Note was deleted or reminder was removed, skipping notification")
                return Result.success()
            }
            
            // Show the notification
            showNotification(noteId, noteTitle, note.content)
            
            // Clear the reminder after it's been triggered
            noteDao.updateReminder(noteId, null)
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing reminder notification", e)
            Result.failure()
        }
    }
    
    private fun showNotification(noteId: Int, title: String, content: String) {
        createNotificationChannel()
        
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
        
        // Convert HTML content to plain text for notification display
        val plainTextContent = stripHtml(content)
        
        // Create intent to open the note when notification is tapped
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTE_ID, noteId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build and show the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(plainTextContent.take(100).let { if (plainTextContent.length > 100) "$it..." else it })
            .setStyle(NotificationCompat.BigTextStyle().bigText(plainTextContent.take(300)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(noteId, notification)
        Log.d(TAG, "Notification shown for note: $noteId")
    }
    
    /**
     * Strips HTML tags from content and returns plain text.
     */
    private fun stripHtml(html: String): String {
        if (html.isBlank()) return ""
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html).toString()
        }.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple whitespaces with single space
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminder_channel_name)
            val description = context.getString(R.string.reminder_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "ReminderWorker"
        const val CHANNEL_ID = "note_reminders"
        const val KEY_NOTE_ID = "note_id"
        const val KEY_NOTE_TITLE = "note_title"
        const val EXTRA_NOTE_ID = "extra_note_id"
        
        /** Unique work name prefix for reminder workers */
        const val WORK_NAME_PREFIX = "reminder_"
        
        fun getWorkName(noteId: Int) = "$WORK_NAME_PREFIX$noteId"
    }
}


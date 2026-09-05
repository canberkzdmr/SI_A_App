package com.cbo.notes.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.logger.AppLogger
import com.cbo.notes.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper responsible for building and displaying reminder notifications.
 * Shared between ReminderWorker (time-based) and GeofenceBroadcastReceiver (location-based).
 */
@Singleton
class ReminderNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Builds and displays a notification for the given note immediately.
     */
    fun showNotification(note: NoteEntity, title: String, triggerType: String) {
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
                AppLogger.w("Notification permission not granted, skipping notification for note: ${note.id}")
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
                context,
                note.id * 10 + 3,
                turnOffIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, context.getString(R.string.notification_action_turn_off_location), turnOffPending)
        } else {
            // Snooze actions
            val snooze15Intent = Intent(context, SnoozeReceiver::class.java).apply {
                action = SnoozeReceiver.ACTION_SNOOZE
                putExtra(EXTRA_NOTE_ID, note.id)
                putExtra(SnoozeReceiver.EXTRA_SNOOZE_MINUTES, 15)
                putExtra(KEY_NOTE_TITLE, title)
            }
            val snooze15Pending = PendingIntent.getBroadcast(
                context,
                note.id * 10 + 1,
                snooze15Intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snooze60Intent = Intent(context, SnoozeReceiver::class.java).apply {
                action = SnoozeReceiver.ACTION_SNOOZE
                putExtra(EXTRA_NOTE_ID, note.id)
                putExtra(SnoozeReceiver.EXTRA_SNOOZE_MINUTES, 60)
                putExtra(KEY_NOTE_TITLE, title)
            }
            val snooze60Pending = PendingIntent.getBroadcast(
                context,
                note.id * 10 + 2,
                snooze60Intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(0, context.getString(R.string.notification_action_snooze_15m), snooze15Pending)
            builder.addAction(0, context.getString(R.string.notification_action_snooze_1h), snooze60Pending)
        }

        if (priorityLevel == "LOW") {
            builder.setSilent(true)
        }

        try {
            NotificationManagerCompat.from(context).notify(note.id, builder.build())
            AppLogger.d("Notification shown successfully for note: ${note.id} (trigger: $triggerType, priority: $priorityLevel)")
        } catch (e: SecurityException) {
            AppLogger.e("SecurityException when posting notification for note ${note.id}", e)
        }
    }

    private fun stripMarkdown(markdown: String): String {
        if (markdown.isBlank()) return ""
        return markdown
            .replace(Regex("[#*`_\\[\\]()]"), "")
            .replace(Regex("!.*\\]\\(.*\\)"), "")
            .replace(Regex("<.*?>"), "")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val highChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                context.getString(R.string.channel_high_priority_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_high_priority_desc)
                enableVibration(true)
                enableLights(true)
            }

            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                context.getString(R.string.channel_default_priority_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_default_priority_desc)
                enableVibration(false)
                enableLights(false)
            }

            notificationManager.createNotificationChannel(highChannel)
            notificationManager.createNotificationChannel(defaultChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_HIGH = "note_reminders_high"
        const val CHANNEL_ID_DEFAULT = "note_reminders_default"
        const val KEY_NOTE_ID = "note_id"
        const val KEY_NOTE_TITLE = "note_title"
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}

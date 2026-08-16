package com.cbo.notes.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cbo.core.database.dao.NoteDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var noteDao: NoteDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return

        val noteId = intent.getIntExtra(ReminderWorker.EXTRA_NOTE_ID, -1)
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 15)
        val noteTitle = intent.getStringExtra(ReminderWorker.KEY_NOTE_TITLE) ?: "Note Reminder"

        if (noteId == -1) return

        // Dismiss notification
        NotificationManagerCompat.from(context).cancel(noteId)

        // Schedule new work
        val snoozeMillis = snoozeMinutes * 60 * 1000L
        val nextTime = System.currentTimeMillis() + snoozeMillis

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // We keep the original repeat and priority but just update the reminderTime
                val note = noteDao.getNoteWithReminder(noteId)
                if (note != null) {
                    noteDao.updateReminder(noteId, nextTime, note.reminderRepeat, note.reminderPriority)
                }

                val inputData = Data.Builder()
                    .putInt(ReminderWorker.KEY_NOTE_ID, noteId)
                    .putString(ReminderWorker.KEY_NOTE_TITLE, noteTitle)
                    .build()

                val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(snoozeMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("note_reminder")
                    .addTag("note_reminder_$noteId")
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    ReminderWorker.getWorkName(noteId),
                    ExistingWorkPolicy.REPLACE,
                    reminderRequest
                )

                Log.d("SnoozeReceiver", "Snoozed note $noteId for $snoozeMinutes minutes")
            } catch (e: Exception) {
                Log.e("SnoozeReceiver", "Error snoozing note", e)
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.cbo.notes.action.SNOOZE"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}

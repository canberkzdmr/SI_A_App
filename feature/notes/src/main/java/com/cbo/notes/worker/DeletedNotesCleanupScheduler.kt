package com.cbo.notes.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler responsible for setting up the periodic deleted notes cleanup work.
 * 
 * The cleanup work runs daily to permanently delete notes that have been in the
 * trash for more than 7 days.
 */
@Singleton
class DeletedNotesCleanupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Schedules the periodic cleanup work to run daily.
     * 
     * Uses [ExistingPeriodicWorkPolicy.KEEP] to prevent rescheduling if work
     * is already scheduled, avoiding duplicate work instances.
     */
    fun schedulePeriodicCleanup() {
        Log.d(TAG, "Scheduling periodic deleted notes cleanup")
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only run when battery is not low
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<DeletedNotesCleanupWorker>(
            repeatInterval = REPEAT_INTERVAL_HOURS,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = FLEX_INTERVAL_HOURS,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DeletedNotesCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
        
        Log.d(TAG, "Periodic cleanup work scheduled successfully")
    }

    /**
     * Cancels the scheduled periodic cleanup work.
     */
    fun cancelPeriodicCleanup() {
        Log.d(TAG, "Cancelling periodic deleted notes cleanup")
        WorkManager.getInstance(context).cancelUniqueWork(DeletedNotesCleanupWorker.WORK_NAME)
    }

    companion object {
        private const val TAG = "DeletedNotesCleanupScheduler"
        private const val WORK_TAG = "deleted_notes_cleanup"
        
        /** Run cleanup every 24 hours */
        private const val REPEAT_INTERVAL_HOURS = 24L
        
        /** Flexible execution window of 6 hours */
        private const val FLEX_INTERVAL_HOURS = 6L
        
        /** Initial backoff delay on failure */
        private const val BACKOFF_DELAY_MINUTES = 15L
    }
}



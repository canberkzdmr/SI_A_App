package com.cbo.notes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cbo.core.common.util.safePutAttribute
import com.cbo.core.common.util.safePutMetric
import com.cbo.core.common.util.traceMetricSuspend
import com.cbo.core.logger.AppLogger
import com.cbo.notes.domain.usecase.CleanupExpiredDeletedNotesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager Worker that periodically cleans up soft-deleted notes that have exceeded
 * the 7-day retention period.
 * 
 * This worker runs daily to permanently remove expired deleted notes from the database.
 */
@HiltWorker
class DeletedNotesCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cleanupExpiredDeletedNotesUseCase: CleanupExpiredDeletedNotesUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = traceMetricSuspend("trace_worker_cleanup_deleted_notes") { trace ->
        trace.safePutMetric("run_attempt_count", runAttemptCount.toLong())
        AppLogger.d("Starting deleted notes cleanup work")
        
        try {
            cleanupExpiredDeletedNotesUseCase.invoke().fold(
                onSuccess = { deletedCount ->
                    trace.safePutMetric("deleted_notes_count", deletedCount.toLong())
                    trace.safePutAttribute("status", "success")
                    AppLogger.d("Successfully cleaned up $deletedCount expired deleted notes")
                    Result.success()
                },
                onFailure = { error ->
                    trace.safePutAttribute("status", "failure")
                    AppLogger.e("Failed to cleanup expired notes: ${error.message}")
                    // Retry if there's an error
                    if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            trace.safePutAttribute("status", "exception")
            AppLogger.e("Exception during cleanup work", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        
        /** Unique name for this worker to prevent duplicate scheduling */
        const val WORK_NAME = "deleted_notes_cleanup_work"
    }
}



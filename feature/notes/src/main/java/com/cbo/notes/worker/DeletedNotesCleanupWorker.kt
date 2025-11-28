package com.cbo.notes.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting deleted notes cleanup work")
        
        return try {
            cleanupExpiredDeletedNotesUseCase.invoke().fold(
                onSuccess = { deletedCount ->
                    Log.d(TAG, "Successfully cleaned up $deletedCount expired deleted notes")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to cleanup expired notes: ${error.message}")
                    // Retry if there's an error
                    if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during cleanup work", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "DeletedNotesCleanupWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        
        /** Unique name for this worker to prevent duplicate scheduling */
        const val WORK_NAME = "deleted_notes_cleanup_work"
    }
}


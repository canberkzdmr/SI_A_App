package com.cbo.core.logger.maintenance

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.cbo.core.logger.api.LoggerConfigProvider
import com.cbo.core.logger.database.LogDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Manages physical disk size limits (default 50MB) and retention cleanup for the log database.
 */
class LogPruningManager(
    private val context: Context,
    private val database: LogDatabase,
    private val configProvider: LoggerConfigProvider,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        private const val DB_FILE_NAME = "app_logs.db"
        private const val BYTES_IN_MB = 1024L * 1024L
        private const val EVICTION_BATCH_COUNT = 5000
    }

    /**
     * Calculates total physical size of database files on disk including WAL and SHM files.
     */
    fun getDatabaseSizeBytes(): Long {
        val dbFile = context.getDatabasePath(DB_FILE_NAME)
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")

        var size = 0L
        if (dbFile.exists()) size += dbFile.length()
        if (walFile.exists()) size += walFile.length()
        if (shmFile.exists()) size += shmFile.length()
        return size
    }

    /**
     * Executes maintenance checks:
     * 1. Retention-based cleanup (older than retentionDays)
     * 2. Size-based cleanup (exceeding maxDbSizeMb)
     * 3. Truncating WAL checkpoint
     */
    fun performMaintenanceAsync() {
        scope.launch {
            try {
                performMaintenanceSync()
            } catch (e: Exception) {
                android.util.Log.e("LogPruningManager", "Maintenance failed", e)
            }
        }
    }

    suspend fun performMaintenanceSync() {
        val dao = database.logDao()

        // 1. Time-based retention cleanup
        val retentionDays = configProvider.retentionDays.value
        if (retentionDays > 0) {
            val cutoffMillis = System.currentTimeMillis() - (retentionDays.toLong() * 24 * 60 * 60 * 1000)
            dao.deleteOlderThan(cutoffMillis)
        }

        // 2. Physical size limit check (FIFO Eviction)
        val maxSizeBytes = configProvider.maxDbSizeMb.value.toLong() * BYTES_IN_MB
        var currentSize = getDatabaseSizeBytes()

        var iterations = 0
        while (currentSize > maxSizeBytes && iterations < 5) {
            val deleted = dao.deleteOldestCount(EVICTION_BATCH_COUNT)
            if (deleted == 0) break
            truncateWalCheckpoint()
            currentSize = getDatabaseSizeBytes()
            iterations++
        }

        // 3. Final WAL truncate
        truncateWalCheckpoint()
    }

    private fun truncateWalCheckpoint() {
        try {
            database.openHelper.writableDatabase.query(
                SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE);")
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    // checkpoint complete
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("LogPruningManager", "WAL checkpoint failed: ${e.message}")
        }
    }
}

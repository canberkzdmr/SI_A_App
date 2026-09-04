package com.cbo.core.logger.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(logs: List<LogEntity>)

    @Query("SELECT * FROM app_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllLogs(limit: Int = 1000): Flow<List<LogEntity>>

    @Query("""
        SELECT * FROM app_logs 
        WHERE (:query IS NULL OR tag LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%')
        AND (:level IS NULL OR level = :level)
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getFilteredLogs(
        query: String?,
        level: String?,
        limit: Int = 1000
    ): Flow<List<LogEntity>>

    @Query("SELECT * FROM app_logs WHERE timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    suspend fun getLogsForExport(fromTimestamp: Long): List<LogEntity>

    @Query("SELECT COUNT(*) FROM app_logs")
    suspend fun getLogCount(): Long

    @Query("DELETE FROM app_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("""
        DELETE FROM app_logs 
        WHERE id IN (
            SELECT id FROM app_logs ORDER BY timestamp ASC LIMIT :count
        )
    """)
    suspend fun deleteOldestCount(count: Int): Int

    @Query("DELETE FROM app_logs")
    suspend fun clearAll()
}

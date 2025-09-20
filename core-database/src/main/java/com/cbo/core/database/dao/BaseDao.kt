package com.cbo.core.database.dao

import androidx.room.*

@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<T>): List<Long>

    @Update
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)
}

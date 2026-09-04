package com.cbo.core.logger.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["level"])
    ]
)
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "level")
    val level: String,

    @ColumnInfo(name = "tag")
    val tag: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "throwable")
    val throwable: String? = null,

    @ColumnInfo(name = "thread_name")
    val threadName: String = "main",

    @ColumnInfo(name = "metadata")
    val metadata: String? = null
)

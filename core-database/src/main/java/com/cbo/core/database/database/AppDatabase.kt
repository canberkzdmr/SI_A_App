package com.cbo.core.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cbo.core.database.converter.Converters
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class, UserDetailEntity::class], 
    version = 1, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun userDetailDao(): UserDetailDao
}

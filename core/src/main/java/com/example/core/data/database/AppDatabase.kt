package com.example.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.core.data.dao.NoteDao
import com.example.core.data.dao.UserDao
import com.example.core.data.model.NoteEntity
import com.example.core.data.model.UserEntity

@Database(entities = [NoteEntity::class, UserEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
}
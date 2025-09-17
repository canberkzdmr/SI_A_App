package com.example.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.core.data.dao.NoteDao
import com.example.core.data.dao.UserDao
import com.example.core.data.dao.UserDetailDao
import com.example.core.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideUserDetailDao(db: AppDatabase): UserDetailDao = db.userDetailDao()
}
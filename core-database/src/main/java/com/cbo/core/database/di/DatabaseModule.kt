package com.cbo.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cbo.core.database.dao.CategoryDao
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.SupportedLanguageDao
import com.cbo.core.database.dao.TagDao
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.database.database.ALL_MIGRATIONS
import com.cbo.core.database.database.AppDatabase
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database",
            ).addMigrations(*ALL_MIGRATIONS)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Populate supported languages when database is first created
                    db.execSQL(
                        """
                        INSERT INTO `supported_languages` (`code`, `displayName`, `nativeName`, `isEnabled`, `sortOrder`)
                        VALUES
                            ('tr', 'Turkish', 'Türkçe', 1, 0),
                            ('en', 'English', 'English', 1, 1),
                            ('de', 'German', 'Deutsch', 0, 2)
                        """.trimIndent()
                    )
                }
            })
            .build()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideUserSettingsDao(database: AppDatabase): UserSettingsDao = database.userSettingsDao()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideUserDetailDao(database: AppDatabase): UserDetailDao = database.userDetailDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()
    
    @Provides
    fun provideSupportedLanguageDao(database: AppDatabase): SupportedLanguageDao = database.supportedLanguageDao()
}

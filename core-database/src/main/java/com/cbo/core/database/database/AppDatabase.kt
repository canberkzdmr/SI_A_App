package com.cbo.core.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cbo.core.database.converter.Converters
import com.cbo.core.database.converter.ViewModeConverter
import com.cbo.core.database.dao.CategoryDao
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.SupportedLanguageDao
import com.cbo.core.database.dao.TagDao
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.database.entity.CategoryEntity
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.core.database.entity.SupportedLanguageEntity
import com.cbo.core.database.entity.TagEntity
import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserSettingsEntity

import com.cbo.core.database.dao.NoteLinkDao
import com.cbo.core.database.dao.NoteTemplateDao
import com.cbo.core.database.entity.NoteLinkEntity
import com.cbo.core.database.entity.NoteTemplateEntity

@Database(
    entities = [
        NoteEntity::class, 
        CategoryEntity::class, 
        TagEntity::class, 
        NoteTagCrossRef::class,
        UserEntity::class, 
        UserDetailEntity::class,
        UserSettingsEntity::class,
        SupportedLanguageEntity::class,
        NoteLinkEntity::class,
        NoteTemplateEntity::class
    ], 
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class, ViewModeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun userDao(): UserDao
    abstract fun userDetailDao(): UserDetailDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun supportedLanguageDao(): SupportedLanguageDao
    abstract fun noteLinkDao(): NoteLinkDao
    abstract fun noteTemplateDao(): NoteTemplateDao
}

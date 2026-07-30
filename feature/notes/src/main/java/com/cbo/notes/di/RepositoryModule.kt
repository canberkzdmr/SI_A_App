package com.cbo.notes.di

import com.cbo.notes.data.repository.CategoryRepositoryImpl
import com.cbo.notes.data.repository.NoteRepositoryImpl
import com.cbo.notes.data.repository.TagRepositoryImpl
import com.cbo.notes.data.repository.UserSettingsRepositoryImpl
import com.cbo.notes.data.repository.NoteLinkRepositoryImpl
import com.cbo.notes.data.repository.NoteTemplateRepositoryImpl
import com.cbo.notes.domain.repository.CategoryRepository
import com.cbo.notes.domain.repository.NoteRepository
import com.cbo.notes.domain.repository.TagRepository
import com.cbo.notes.domain.repository.UserSettingsRepository
import com.cbo.notes.domain.repository.NoteLinkRepository
import com.cbo.notes.domain.repository.NoteTemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindNoteLinkRepository(
        noteLinkRepositoryImpl: NoteLinkRepositoryImpl
    ): NoteLinkRepository

    @Binds
    @Singleton
    abstract fun bindNoteTemplateRepository(
        noteTemplateRepositoryImpl: NoteTemplateRepositoryImpl
    ): NoteTemplateRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        userSettingsRepositoryImpl: UserSettingsRepositoryImpl
    ): UserSettingsRepository
}

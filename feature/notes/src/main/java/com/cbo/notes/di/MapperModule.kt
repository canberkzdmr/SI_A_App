package com.cbo.notes.di

import com.cbo.notes.data.mapper.CategoryEntityMapper
import com.cbo.notes.data.mapper.NoteEntityMapper
import com.cbo.notes.data.mapper.TagEntityMapper
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideCategoryEntityMapper(): CategoryEntityMapper {
        return CategoryEntityMapper()
    }

    @Provides
    @Singleton
    fun provideTagEntityMapper(): TagEntityMapper {
        return TagEntityMapper()
    }

    @Provides
    @Singleton
    fun provideNoteEntityMapper(
        categoryEntityMapper: CategoryEntityMapper,
        tagEntityMapper: TagEntityMapper
    ): NoteEntityMapper {
        return NoteEntityMapper(categoryEntityMapper, tagEntityMapper)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}


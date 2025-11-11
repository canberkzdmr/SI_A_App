package com.cbo.core.data.di

import com.cbo.core.data.mapper.SupportedLanguageEntityMapper
import com.cbo.core.data.mapper.UserEntityMapper
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
    fun provideUserEntityMapper(): UserEntityMapper {
        return UserEntityMapper()
    }

    @Provides
    @Singleton
    fun provideSupportedLanguageEntityMapper(): SupportedLanguageEntityMapper {
        return SupportedLanguageEntityMapper()
    }
}

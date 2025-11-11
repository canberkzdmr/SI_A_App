package com.cbo.core.data.di

import com.cbo.core.data.repository.SupportedLanguageRepositoryImpl
import com.cbo.core.domain.repository.SupportedLanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SupportedLanguageModule {
    
    @Binds
    @Singleton
    abstract fun bindSupportedLanguageRepository(
        impl: SupportedLanguageRepositoryImpl
    ): SupportedLanguageRepository
}
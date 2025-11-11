package com.cbo.core.data.di

import com.cbo.core.data.locale.LocaleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocaleModule {

    @Provides
    @Singleton
    fun provideLocaleManager(): LocaleManager {
        return LocaleManager()
    }
}


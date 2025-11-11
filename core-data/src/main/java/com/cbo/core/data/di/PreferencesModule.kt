package com.cbo.core.data.di

import android.content.Context
import com.cbo.core.data.prefs.LanguagePreferencesManager
import com.cbo.core.data.repository.PreferencesRepositoryImpl
import com.cbo.core.domain.preferences.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideLanguagePreferencesManager(
            @ApplicationContext context: Context
        ): LanguagePreferencesManager {
            return LanguagePreferencesManager(context)
        }
    }
}
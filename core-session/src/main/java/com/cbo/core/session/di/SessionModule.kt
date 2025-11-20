package com.cbo.core.session.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.cbo.core.session.data.datastore.SessionManager
import com.cbo.core.session.data.repository.SessionRepositoryImpl
import com.cbo.core.session.domain.repository.SessionRepository
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }

        @Provides
        @Singleton
        fun provideSessionManager(dataStore: DataStore<Preferences>): SessionManager {
            return SessionManager(dataStore)
        }

        @Provides
        @Singleton
        fun provideGetActiveUserUseCase(
            sessionRepository: SessionRepository
        ): GetActiveUserUseCase {
            return GetActiveUserUseCase(sessionRepository)
        }
    }
}

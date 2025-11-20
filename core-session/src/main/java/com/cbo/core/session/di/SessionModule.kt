package com.cbo.core.session.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
        fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                "secure_session_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        @Provides
        @Singleton
        fun provideSessionManager(sharedPreferences: SharedPreferences): SessionManager {
            return SessionManager(sharedPreferences)
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

package com.cbo.core.session.di

import com.cbo.core.session.UserSession
import com.cbo.core.session.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserSessionModule {

    @Provides
    @Singleton
    fun provideUserSession(
        sessionRepository: SessionRepository
    ): UserSession {
        return UserSession(sessionRepository)
    }
}

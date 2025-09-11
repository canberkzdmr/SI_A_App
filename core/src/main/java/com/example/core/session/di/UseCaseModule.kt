package com.example.core.session.di

import com.example.core.data.dao.UserDao
import com.example.core.domain.usecase.VerifyPasswordUseCase
import com.example.core.session.domain.repository.SessionRepository
import com.example.core.session.domain.usecase.LoginUseCase
import com.example.core.session.domain.usecase.LogoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(
        sessionRepository: SessionRepository,
        verifyPasswordUseCase: VerifyPasswordUseCase,
        userDao: UserDao
    ): LoginUseCase {
        return LoginUseCase(userDao, verifyPasswordUseCase, sessionRepository)
    }

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        sessionRepository: SessionRepository
    ): LogoutUseCase {
        return LogoutUseCase(sessionRepository)
    }
}
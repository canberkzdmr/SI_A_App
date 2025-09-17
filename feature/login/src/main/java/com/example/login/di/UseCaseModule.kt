package com.example.login.di

import com.example.core.data.dao.UserDao
import com.example.core.data.mapper.UserEntityMapper
import com.example.core.domain.usecase.VerifyPasswordUseCase
import com.example.core.session.domain.repository.SessionRepository
import com.example.login.domain.usecase.LoginUseCase
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
        userDao: UserDao,
        userEntityMapper: UserEntityMapper
    ): LoginUseCase {
        return LoginUseCase(
            userDao,
            verifyPasswordUseCase,
            sessionRepository,
            userEntityMapper
        )
    }
}
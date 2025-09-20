package com.cbo.login.di

import com.cbo.core.database.dao.UserDao
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.session.domain.repository.SessionRepository
import com.cbo.login.domain.usecase.LoginUseCase
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
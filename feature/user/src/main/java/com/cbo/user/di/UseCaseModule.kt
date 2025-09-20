package com.cbo.user.di

import com.cbo.user.domain.repository.UserRepository
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.user.domain.usecase.VerifyCurrentPasswordUseCase
import com.cbo.user.domain.usecase.ChangePasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetUserWithDetailUseCase(repository: UserRepository): GetUserWithDetailUseCase {
        return GetUserWithDetailUseCase(repository)
    }

    @Provides
    fun provideVerifyCurrentPasswordUseCase(repository: UserRepository): VerifyCurrentPasswordUseCase {
        return VerifyCurrentPasswordUseCase(repository)
    }

    @Provides
    fun provideChangePasswordUseCase(
        repository: UserRepository,
        verifyCurrentPasswordUseCase: VerifyCurrentPasswordUseCase
    ): ChangePasswordUseCase {
        return ChangePasswordUseCase(repository, verifyCurrentPasswordUseCase)
    }
}
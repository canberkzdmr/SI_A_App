package com.example.core.domain.di

import com.example.core.domain.repository.UserRepository
import com.example.core.domain.usecase.VerifyPasswordUseCase
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
    fun provideVerifyPasswordUseCase(
        userRepository: UserRepository
    ): VerifyPasswordUseCase {
        return VerifyPasswordUseCase(userRepository)
    }
}

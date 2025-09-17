package com.cbo.user.di

import com.cbo.user.domain.repository.UserRepository
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
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
}
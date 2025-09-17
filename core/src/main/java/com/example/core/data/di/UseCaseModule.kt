package com.example.core.data.di

import com.example.core.domain.usecase.GetActiveUserUseCase
import com.example.core.session.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetActiveUserUseCase(sessionRepository: SessionRepository): GetActiveUserUseCase =
        GetActiveUserUseCase(sessionRepository)
}
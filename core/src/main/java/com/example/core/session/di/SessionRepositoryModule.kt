package com.example.core.session.di

import com.example.core.session.data.repository.SessionRepositoryImpl
import com.example.core.session.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}
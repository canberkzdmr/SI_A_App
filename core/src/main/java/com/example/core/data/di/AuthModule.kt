package com.example.core.data.di

import com.example.core.data.dao.UserDao
import com.example.core.data.repository.UserRepositoryImpl
import com.example.core.domain.repository.UserRepository
import com.example.core.domain.usecase.VerifyPasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao)

    @Provides
    fun provideVerifyPasswordUseCase(repository: UserRepository): VerifyPasswordUseCase =
        VerifyPasswordUseCase(repository)
}
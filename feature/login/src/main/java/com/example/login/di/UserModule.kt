package com.example.login.di

import com.example.core.data.dao.UserDao
import com.example.login.data.repository.UserRepositoryImpl
import com.example.login.domain.repository.UserRepository
import com.example.login.domain.usecase.GetUserUseCase
import com.example.login.domain.usecase.RegisterUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Provides
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao)

    @Provides
    fun provideRegisterUserUseCase(repository: UserRepository): RegisterUserUseCase = RegisterUserUseCase(repository)

    @Provides
    fun provideGetUserUseCase(repository: UserRepository): GetUserUseCase = GetUserUseCase(repository)
}

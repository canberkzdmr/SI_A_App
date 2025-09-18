package com.example.login.di

import com.example.core.database.dao.UserDao
import com.example.core.data.mapper.UserEntityMapper
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
    fun provideUserRepository(userDao: UserDao, userEntityMapper: UserEntityMapper): UserRepository = UserRepositoryImpl(userDao, userEntityMapper)

    @Provides
    fun provideRegisterUserUseCase(repository: UserRepository): RegisterUserUseCase = RegisterUserUseCase(repository)

    @Provides
    fun provideGetUserUseCase(userDao: UserDao, userEntityMapper: UserEntityMapper): GetUserUseCase = GetUserUseCase(userDao, userEntityMapper)
}

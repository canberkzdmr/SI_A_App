package com.cbo.login.di

import com.cbo.core.database.dao.UserDao
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.database.database.AppDatabase
import com.cbo.login.data.repository.UserRepositoryImpl
import com.cbo.login.domain.repository.UserRepository
import com.cbo.login.domain.usecase.GetUserUseCase
import com.cbo.login.domain.usecase.RegisterUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Provides
    fun provideUserRepository(userDao: UserDao, userSettingsDao: UserSettingsDao, userEntityMapper: UserEntityMapper, db: AppDatabase): UserRepository = UserRepositoryImpl(userDao, userSettingsDao, userEntityMapper, db)

    @Provides
    fun provideRegisterUserUseCase(repository: UserRepository): RegisterUserUseCase = RegisterUserUseCase(repository)

    @Provides
    fun provideGetUserUseCase(userDao: UserDao, userEntityMapper: UserEntityMapper): GetUserUseCase = GetUserUseCase(userDao, userEntityMapper)
}

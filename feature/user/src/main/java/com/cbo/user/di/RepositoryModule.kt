package com.cbo.user.di

import com.cbo.user.data.repository.UserRepositoryImpl
import com.cbo.user.domain.repository.UserRepository
import com.example.core.data.dao.UserDao
import com.example.core.data.dao.UserDetailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, userDetailDao: UserDetailDao): UserRepository = UserRepositoryImpl(userDao, userDetailDao)
}

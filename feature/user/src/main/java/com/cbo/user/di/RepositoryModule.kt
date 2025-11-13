package com.cbo.user.di

import com.cbo.core.data.mapper.UserDetailEntityMapper
import com.cbo.core.data.mapper.UserWithDetailEntityMapper
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.user.data.repository.UserRepositoryImpl
import com.cbo.user.domain.repository.UserRepository
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
    fun provideUserRepository(
        userDao: UserDao,
        userDetailDao: UserDetailDao,
        userWithDetailMapper: UserWithDetailEntityMapper,
        userDetailMapper: UserDetailEntityMapper,
    ): UserRepository = UserRepositoryImpl(
        userDao,
        userDetailDao,
        userWithDetailMapper,
        userDetailMapper
    )
}

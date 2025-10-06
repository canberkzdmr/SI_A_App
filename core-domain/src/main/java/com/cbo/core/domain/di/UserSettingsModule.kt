package com.cbo.core.domain.di

import com.cbo.core.domain.repository.UserSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserSettingsModule {

    /*@Provides
    @Singleton
    fun provideUserSettingsRepository(
        dao: UserSettingsDao
    ): UserSettingsRepository = UserSettingsRepositoryImpl(dao)*/
}
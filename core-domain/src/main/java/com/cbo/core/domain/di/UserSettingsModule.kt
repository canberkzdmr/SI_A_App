package com.cbo.core.domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserSettingsModule {

    /*@Provides
    @Singleton
    fun provideUserSettingsRepository(
        dao: UserSettingsDao
    ): UserSettingsRepository = UserSettingsRepositoryImpl(dao)*/
}
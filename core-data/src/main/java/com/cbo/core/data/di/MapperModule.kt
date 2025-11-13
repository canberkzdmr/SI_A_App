package com.cbo.core.data.di

import com.cbo.core.data.mapper.SupportedLanguageEntityMapper
import com.cbo.core.data.mapper.UserDetailEntityMapper
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.data.mapper.UserSettingsEntityMapper
import com.cbo.core.data.mapper.UserWithDetailEntityMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideUserEntityMapper(): UserEntityMapper {
        return UserEntityMapper()
    }

    @Provides
    @Singleton
    fun provideUserDetailEntityMapper(): UserDetailEntityMapper {
        return UserDetailEntityMapper()
    }

    @Provides
    @Singleton
    fun provideUserSettingsEntityMapper(): UserSettingsEntityMapper {
        return UserSettingsEntityMapper()
    }

    @Provides
    @Singleton
    fun provideUserWithDetailEntityMapper(
        userMapper: UserEntityMapper,
        userDetailMapper: UserDetailEntityMapper,
        userSettingsMapper: UserSettingsEntityMapper,
    ): UserWithDetailEntityMapper {
        return UserWithDetailEntityMapper(userMapper, userDetailMapper, userSettingsMapper)
    }

    @Provides
    @Singleton
    fun provideSupportedLanguageEntityMapper(): SupportedLanguageEntityMapper {
        return SupportedLanguageEntityMapper()
    }
}

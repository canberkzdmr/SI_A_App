package com.cbo.core.domain.di

import com.cbo.core.domain.repository.SupportedLanguageRepository
import com.cbo.core.domain.repository.UserRepository
import com.cbo.core.domain.repository.UserSettingsRepository
import com.cbo.core.domain.usecase.GetUserSettingsUseCase
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.domain.usecase.SetFirstLoginDoneUseCase
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.domain.usecase.language.GetAllSupportedLanguagesUseCase
import com.cbo.core.domain.usecase.language.GetSupportedLanguagesSyncUseCase
import com.cbo.core.domain.usecase.language.GetSupportedLanguagesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideVerifyPasswordUseCase(
        userRepository: UserRepository
    ): VerifyPasswordUseCase {
        return VerifyPasswordUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserSettingsUseCase(
        userSettingsRepository: UserSettingsRepository
    ): GetUserSettingsUseCase {
        return GetUserSettingsUseCase(userSettingsRepository)
    }

    @Provides
    @Singleton
    fun provideSetFirstLoginDoneUseCase(
        userSettingsRepository: UserSettingsRepository
    ): SetFirstLoginDoneUseCase {
        return SetFirstLoginDoneUseCase(userSettingsRepository)
    }

    @Provides
    @Singleton
    fun provideSetBiometricEnabledUseCase(
        userSettingsRepository: UserSettingsRepository
    ): SetBiometricEnabledUseCase {
        return SetBiometricEnabledUseCase(userSettingsRepository)
    }

    @Provides
    fun provideGetSupportedLanguagesUseCase(
        repository: SupportedLanguageRepository
    ): GetSupportedLanguagesUseCase {
        return GetSupportedLanguagesUseCase(repository)
    }

    @Provides
    fun provideGetAllSupportedLanguagesUseCase(
        repository: SupportedLanguageRepository
    ): GetAllSupportedLanguagesUseCase {
        return GetAllSupportedLanguagesUseCase(repository)
    }

    @Provides
    fun provideGetSupportedLanguagesSyncUseCase(
        repository: SupportedLanguageRepository
    ): GetSupportedLanguagesSyncUseCase {
        return GetSupportedLanguagesSyncUseCase(repository)
    }
}

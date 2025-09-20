package com.cbo.ui.di

import com.cbo.ui.snackbar.SnackbarManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiModule {

    @Provides
    @Singleton
    fun provideSnackbarManager(): SnackbarManager = SnackbarManager
}

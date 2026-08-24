package com.cbo.notes.di

import android.content.Context
import com.cbo.notes.data.repository.GooglePlacesSearchRepositoryImpl
import com.cbo.notes.domain.repository.LocationSearchRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
            val apiKey = applicationInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initialize(context, apiKey)
        }
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationSearchRepository(
        placesClient: PlacesClient
    ): LocationSearchRepository {
        return GooglePlacesSearchRepositoryImpl(placesClient)
    }
}

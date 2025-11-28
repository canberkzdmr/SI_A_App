package com.cbo.memcloud

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cbo.notes.worker.DeletedNotesCleanupScheduler
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var deletedNotesCleanupScheduler: DeletedNotesCleanupScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyApp onCreate")
        
        // Load and apply saved language on app start
        loadSavedLanguage()
        
        // Schedule periodic cleanup of deleted notes
        scheduleDeletedNotesCleanup()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Log.d(TAG, "MyApp attachBaseContext")
    }

    private fun loadSavedLanguage() {
        try {
            val prefs = getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
            val savedLanguage = prefs.getString(KEY_CURRENT_LANGUAGE, null)
            
            Log.d(TAG, "Loading saved language: $savedLanguage")
            
            if (!savedLanguage.isNullOrEmpty()) {
                val locale = Locale.forLanguageTag(savedLanguage)
                val localeList = LocaleListCompat.create(locale)
                Log.d(TAG, "Setting application locales to: $savedLanguage")
                AppCompatDelegate.setApplicationLocales(localeList)
                
                // Verify
                val current = AppCompatDelegate.getApplicationLocales()
                Log.d(TAG, "Current application locales: ${if (current.isEmpty) "empty" else current[0]?.toLanguageTag()}")
            } else {
                Log.d(TAG, "No saved language found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved language", e)
        }
    }

    private fun scheduleDeletedNotesCleanup() {
        try {
            deletedNotesCleanupScheduler.schedulePeriodicCleanup()
            Log.d(TAG, "Deleted notes cleanup scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling deleted notes cleanup", e)
        }
    }

    companion object {
        private const val TAG = "MyApp"
        private const val LANGUAGE_PREFS_NAME = "language_preferences"
        private const val KEY_CURRENT_LANGUAGE = "current_language"
    }
}
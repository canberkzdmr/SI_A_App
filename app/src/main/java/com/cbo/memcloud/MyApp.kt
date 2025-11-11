package com.cbo.memcloud

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyApp onCreate")
        
        // Load and apply saved language on app start
        loadSavedLanguage()
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

    companion object {
        private const val TAG = "MyApp"
        private const val LANGUAGE_PREFS_NAME = "language_preferences"
        private const val KEY_CURRENT_LANGUAGE = "current_language"
    }
}
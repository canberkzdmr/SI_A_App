package com.cbo.core.data.locale

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages application locale changes.
 * Centralizes per-app language settings via AppCompatDelegate.
 */
@Singleton
class LocaleManager @Inject constructor() {

    /**
     * Sets the application locale to the specified language code.
     * If languageCode is null, the system default locale will be used.
     *
     * @param languageCode ISO 639-1 language code (e.g., "en", "tr") or null for system default
     */
    fun setAppLocale(languageCode: String?) {
        try {
            Log.d(TAG, "=== setAppLocale called with languageCode: $languageCode ===")
            val localeList = if (languageCode.isNullOrEmpty()) {
                // Clear to system default
                Log.d(TAG, "Clearing custom locale via AppCompatDelegate")
                LocaleListCompat.getEmptyLocaleList()
            } else {
                val locale = Locale.forLanguageTag(languageCode)
                Log.d(TAG, "Setting AppCompatDelegate locales to: $languageCode (${locale.displayLanguage})")
                // Also set process default for libraries that read Locale.getDefault()
                Locale.setDefault(locale)
                LocaleListCompat.create(locale)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
            val current = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "=== setAppLocale completed, current AppCompatDelegate locale: ${if (current.isEmpty) "system" else current[0]?.toLanguageTag()} ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app locale to $languageCode", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Deprecated: No longer needed when using AppCompatDelegate per-app languages.
     */
    fun updateConfiguration(context: Context, languageCode: String?): Context = context

    /**
     * Gets the current application locale.
     *
     * @return The current language code or null if using system default
     */
    fun getCurrentLocale(): String? {
        return try {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) {
                null
            } else {
                locales[0]?.toLanguageTag()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current locale", e)
            null
        }
    }

    companion object {
        private const val TAG = "LocaleManager"
    }
}


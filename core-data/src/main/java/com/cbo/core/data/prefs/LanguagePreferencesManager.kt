package com.cbo.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages language preferences using SharedPreferences for synchronous access.
 * This is separate from the database to allow immediate locale loading on app startup.
 */
@Singleton
class LanguagePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Saves the language preference for a specific user.
     * This is called after successfully saving to the database.
     */
    fun saveLanguagePreference(userId: Int, languageCode: String?) {
        prefs.edit()
            .putString(KEY_LANGUAGE_PREFIX + userId, languageCode ?: DEFAULT_LANGUAGE)
            .apply()
        
        // Also save as current user's language for quick access
        prefs.edit()
            .putString(KEY_CURRENT_LANGUAGE, languageCode ?: DEFAULT_LANGUAGE)
            .putInt(KEY_CURRENT_USER_ID, userId)
            .apply()
    }

    /**
     * Gets the saved language preference for a specific user.
     * Returns synchronously for immediate use on app startup.
     */
    fun getLanguagePreference(userId: Int): String {
        return prefs.getString(KEY_LANGUAGE_PREFIX + userId, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Gets the current user's language preference.
     * This is used on app startup before user session is fully loaded.
     */
    fun getCurrentLanguagePreference(): String? {
        return prefs.getString(KEY_CURRENT_LANGUAGE, null)
    }

    /**
     * Gets the current user ID whose language preference is cached.
     */
    fun getCurrentUserId(): Int {
        return prefs.getInt(KEY_CURRENT_USER_ID, -1)
    }

    /**
     * Clears the language preference for a specific user (called on logout).
     */
    fun clearLanguagePreference(userId: Int) {
        prefs.edit()
            .remove(KEY_LANGUAGE_PREFIX + userId)
            .apply()
    }

    /**
     * Clears all language preferences.
     */
    fun clearAll() {
        prefs.edit()
            .clear()
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "language_preferences"
        private const val KEY_LANGUAGE_PREFIX = "language_"
        private const val KEY_CURRENT_LANGUAGE = "current_language"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val DEFAULT_LANGUAGE = "en"
    }
}


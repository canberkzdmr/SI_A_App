package com.cbo.core.session.data.datastore

import android.content.SharedPreferences
import android.util.Log
import com.cbo.core.session.domain.model.Session
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class SessionManager @Inject constructor(
    private val preferences: SharedPreferences
) {

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    fun saveSession(session: Session) {
        // Don't log sensitive data in production
        // Log.d("SessionManager", "saveSession -> $session")
        preferences.edit().apply {
            putInt(KEY_USER_ID, session.userId)
            putString(KEY_USERNAME, session.username)
            putString(KEY_EMAIL, session.email)
            apply()
        }
    }

    val currentSession: Flow<Session?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_USER_ID || key == KEY_USERNAME || key == KEY_EMAIL) {
                trySend(getSessionFromPrefs(prefs))
            }
        }
        
        preferences.registerOnSharedPreferenceChangeListener(listener)
        
        // Send initial value
        trySend(getSessionFromPrefs(preferences))
        
        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun getSessionFromPrefs(prefs: SharedPreferences): Session? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        val username = prefs.getString(KEY_USERNAME, null)
        val email = prefs.getString(KEY_EMAIL, null)

        return if (id != -1 && username != null && email != null) {
            Session(id, username, email)
        } else {
            null
        }
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}

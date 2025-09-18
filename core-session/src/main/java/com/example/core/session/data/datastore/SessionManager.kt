package com.example.core.session.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.core.session.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL = stringPreferencesKey("email")
    }

    suspend fun saveSession(session: Session) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_USERNAME] = session.username
            prefs[KEY_EMAIL] = session.email
        }
    }

    val currentSession: Flow<Session?> = dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        val username = prefs[KEY_USERNAME] ?: return@map null
        val email = prefs[KEY_EMAIL] ?: return@map null
        Session(id, username, email)
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}

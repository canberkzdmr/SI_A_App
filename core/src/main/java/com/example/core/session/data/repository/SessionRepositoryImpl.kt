package com.example.core.session.data.repository

import com.example.core.session.data.datastore.SessionManager
import com.example.core.session.domain.model.Session
import com.example.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager
) : SessionRepository {

    override suspend fun saveSession(session: Session) {
        sessionManager.saveSession(session)
    }

    override fun getSession(): Flow<Session?> = sessionManager.currentSession

    override suspend fun clearSession() {
        sessionManager.clearSession()
    }
}
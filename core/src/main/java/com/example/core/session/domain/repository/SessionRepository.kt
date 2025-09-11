package com.example.core.session.domain.repository

import com.example.core.session.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun saveSession(session: Session)
    fun getSession(): Flow<Session?>
    suspend fun clearSession()
}
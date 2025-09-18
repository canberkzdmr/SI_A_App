package com.example.core.session.domain.repository

import com.example.core.domain.model.User
import com.example.core.session.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val activeUser: Flow<User?>
    val currentSession: Flow<Session?>
    
    suspend fun saveSession(session: Session)
    suspend fun clearSession()
    suspend fun setActiveUser(userEntity: com.example.core.database.entity.UserEntity)
}

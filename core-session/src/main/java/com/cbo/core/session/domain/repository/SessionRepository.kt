package com.cbo.core.session.domain.repository

import com.cbo.core.domain.model.User
import com.cbo.core.session.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val activeUser: Flow<User?>
    val currentSession: Flow<Session?>
    
    suspend fun saveSession(session: Session)
    suspend fun clearSession()
    suspend fun setActiveUser(userEntity: com.cbo.core.database.entity.UserEntity)
}

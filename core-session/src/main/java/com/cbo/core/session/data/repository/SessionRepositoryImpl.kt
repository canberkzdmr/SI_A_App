package com.cbo.core.session.data.repository

import com.cbo.core.domain.model.User
import com.cbo.core.session.data.datastore.SessionManager
import com.cbo.core.session.domain.model.Session
import com.cbo.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager
    // TODO: Add UserRepository dependency to get full user details
    // private val userRepository: UserRepository
) : SessionRepository {

    override val currentSession: Flow<Session?> = sessionManager.currentSession

    override val activeUser: Flow<User?> = currentSession.map { session ->
        session?.let {
            // TODO: Get full user details from UserRepository
            User(
                id = it.userId,
                username = it.username,
                email = it.email,
                isActive = true
            )
        }
    }

    override suspend fun saveSession(session: Session) {
        sessionManager.saveSession(session)
    }

    override suspend fun clearSession() {
        sessionManager.clearSession()
    }

    override suspend fun setActiveUser(userEntity: com.cbo.core.database.entity.UserEntity) {
        val session = Session(
            userId = userEntity.id,
            username = userEntity.username,
            email = userEntity.email
        )
        sessionManager.saveSession(session)
    }
}

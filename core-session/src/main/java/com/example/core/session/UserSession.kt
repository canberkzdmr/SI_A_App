package com.example.core.session

import com.example.core.domain.model.User
import com.example.core.session.domain.model.Session
import com.example.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserSession wrapper that provides a simplified interface for feature modules
 * to interact with user session state. This acts as a facade over SessionRepository.
 */
@Singleton
class UserSession @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val currentUser: Flow<User?> = sessionRepository.activeUser
    
    fun setUser(user: User) {
        scope.launch {
            val session = Session(
                userId = user.id,
                username = user.username,
                email = user.email
            )
            sessionRepository.saveSession(session)
        }
    }
    
    fun clearUser() {
        scope.launch {
            sessionRepository.clearSession()
        }
    }
}

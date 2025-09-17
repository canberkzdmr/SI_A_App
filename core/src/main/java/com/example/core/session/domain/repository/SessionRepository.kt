package com.example.core.session.domain.repository

import com.example.core.data.model.UserEntity
import com.example.core.domain.model.User
import com.example.core.session.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    /** Observe the currently active user (null if not logged in). */
    val activeUser: Flow<User?>

    /** Mark a user as the active session. */
    suspend fun setActiveUser(userEntity: UserEntity)

    /** Clear the active user (logout). */
    suspend fun clearActiveUser()
}
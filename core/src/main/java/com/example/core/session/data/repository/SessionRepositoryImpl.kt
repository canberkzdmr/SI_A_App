package com.example.core.session.data.repository

import com.example.core.data.dao.UserDao
import com.example.core.data.mapper.UserEntityMapper
import com.example.core.data.model.UserEntity
import com.example.core.domain.model.User
import com.example.core.session.data.datastore.SessionManager
import com.example.core.session.domain.model.Session
import com.example.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userEntityMapper: UserEntityMapper
): SessionRepository {

    // Flow of active user (null if none)
    override val activeUser: Flow<User?> = userDao.getActiveUserFlow()
        .map { entity -> entity?.let { userEntityMapper.toDomain(it) } }

    // Mark user as logged in
    override suspend fun setActiveUser(userEntity: UserEntity) {
        // Optional: deactivate all others
        userDao.deactivateAllUsers()
        userDao.updateUser(userEntity.copy(isActive = true))
    }

    // Logout
    override suspend fun clearActiveUser() {
        userDao.deactivateAllUsers()
    }
}
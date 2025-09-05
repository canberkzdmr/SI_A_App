package com.example.login.data.repository

import android.util.Log
import com.example.core.data.dao.UserDao
import com.example.core.data.model.UserEntity
import com.example.login.domain.model.User
import com.example.login.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val userDao: UserDao,
    ) : UserRepository {
        override suspend fun registerUser(user: User): Boolean {
            return try {
                val entity =
                    UserEntity(
                        username = user.username,
                        password = user.password,
                        email = user.email,
                        lastPasswordChangeDate = user.lastPasswordChangeDate,
                        registrationDate = user.registerDate,
                    )
                userDao.insert(entity)
                return true
            } catch (e: Exception) {
                Log.e("UserRepositoryImpl", "An error occurred -> ${e.message}")
                return false
            }
        }

        override suspend fun getUser(username: String): User? {
            return try {
                return userDao.getUserByUsername(username)?.let {
                    User(
                        id = it.id,
                        username = it.username,
                        password = it.password,
                        email = it.email,
                        lastPasswordChangeDate = it.lastPasswordChangeDate,
                        registerDate = it.registrationDate,
                    )
                }
            } catch (e: Exception) {
                Log.e("UserRepositoryImpl", "An error occurred -> ${e.message}")
                return null
            }
        }
    }

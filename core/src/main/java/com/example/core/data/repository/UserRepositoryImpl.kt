package com.example.core.data.repository

import com.example.core.data.dao.UserDao
import com.example.core.data.model.UserEntity
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val userdao: UserDao,
    ) : UserRepository {
        override suspend fun registerUser(
            username: String,
            password: String,
            email: String,
        ): Boolean =
            try {
                userdao.insert(
                    UserEntity(
                        username = username,
                        password = password,
                        email = email,
                        lastPasswordChangeDate = "",
                        registrationDate = "",
                    ),
                )
                true
            } catch (e: Exception) {
                false
            }
    }

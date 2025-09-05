package com.example.login.domain.repository

import com.example.login.domain.model.User

interface UserRepository {
    suspend fun registerUser(user: User): Boolean
    suspend fun getUser(username: String): User?
}
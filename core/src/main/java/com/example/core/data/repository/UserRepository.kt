package com.example.core.data.repository

interface UserRepository {
    suspend fun registerUser(
        username: String,
        password: String,
        email: String
    ): Boolean
}
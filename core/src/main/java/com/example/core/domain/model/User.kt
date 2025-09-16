package com.example.core.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
)

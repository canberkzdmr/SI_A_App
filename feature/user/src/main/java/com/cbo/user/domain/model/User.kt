package com.cbo.user.domain.model

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val email: String,
)

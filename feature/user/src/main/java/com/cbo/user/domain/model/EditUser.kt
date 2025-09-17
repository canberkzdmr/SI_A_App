package com.cbo.user.domain.model

data class EditUser(
    val id: Int,
    val username: String,
    val password: String,
    val email: String,
    val lastLoginDate: String,
    val registerDate: String,
)

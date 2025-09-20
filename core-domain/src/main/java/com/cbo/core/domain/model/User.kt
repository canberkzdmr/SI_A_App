package com.cbo.core.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val isActive: Boolean = false,
)

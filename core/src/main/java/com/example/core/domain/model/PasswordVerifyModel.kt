package com.example.core.domain.model

data class PasswordVerifyModel(
    val passwordHash: ByteArray,
    val salt: ByteArray
)

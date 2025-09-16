package com.example.login.domain.model

import com.example.core.data.model.UserEntity

// Data (Entity) → Domain
fun UserEntity.toDomain(): User =
    User(
        id = id,
        username = username,
        email = email,
        password = "",
        lastPasswordChangeDate = lastPasswordChangeDate,
        registerDate = registrationDate,
        termsAndConditionsChecked = true
    )

// Domain → Data (for inserting into DB)
fun User.toEntity(passwordHash: ByteArray, salt: ByteArray): UserEntity = UserEntity(
    id = id,
    username = username,
    passwordHash = passwordHash,
    salt = salt,
    email = email,
    registrationDate = registerDate,
    lastPasswordChangeDate = lastPasswordChangeDate
)

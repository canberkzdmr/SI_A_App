package com.cbo.login.domain.model

import com.cbo.core.database.entity.UserEntity

// Data (Entity) → Domain
fun UserEntity.toDomain(): RegisterUserModel =
    RegisterUserModel(
        id = id,
        username = username,
        email = email,
        password = "",
        retypePassword = "",
        lastPasswordChangeDate = lastPasswordChangeDate,
        registerDate = registrationDate,
        termsAndConditionsChecked = true
    )

// Domain → Data (for inserting into DB)
fun RegisterUserModel.toEntity(passwordHash: ByteArray, salt: ByteArray): UserEntity = UserEntity(
    id = id,
    username = username,
    passwordHash = passwordHash,
    salt = salt,
    email = email,
    registrationDate = registerDate,
    lastPasswordChangeDate = lastPasswordChangeDate,
    isActive = true,
)

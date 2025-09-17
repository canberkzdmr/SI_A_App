package com.example.core.data.mapper

import com.example.core.data.model.UserEntity
import com.example.core.domain.model.User
import javax.inject.Inject

class UserEntityMapper @Inject constructor() {

    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            isActive = entity.isActive,
            avatarUrl = null // You can map from another source if you have avatars
        )
    }

    fun toEntity(domain: User, passwordHash: ByteArray, salt: ByteArray): UserEntity {
        return UserEntity(
            id = domain.id,
            username = domain.username,
            passwordHash = passwordHash,
            salt = salt,
            email = domain.email,
            registrationDate = "", // You need to provide this from the domain or repository
            lastPasswordChangeDate = "", // Same here
            isActive = domain.isActive
        )
    }
}
package com.cbo.core.data.mapper

import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.model.User
import javax.inject.Inject

class UserEntityMapper @Inject constructor() {
    
    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            isActive = entity.isActive
        )
    }
    
    fun toEntity(domain: User, passwordHash: ByteArray, salt: ByteArray, registrationDate: String, lastPasswordChangeDate: String): UserEntity {
        return UserEntity(
            id = domain.id,
            username = domain.username,
            passwordHash = passwordHash,
            salt = salt,
            email = domain.email,
            registrationDate = registrationDate,
            lastPasswordChangeDate = lastPasswordChangeDate,
            isActive = domain.isActive
        )
    }
}

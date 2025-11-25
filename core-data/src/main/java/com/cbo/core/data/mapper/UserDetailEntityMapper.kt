package com.cbo.core.data.mapper

import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.domain.model.Gender
import com.cbo.core.domain.model.UserDetail
import javax.inject.Inject

class UserDetailEntityMapper @Inject constructor() {

    fun toDomain(entity: UserDetailEntity): UserDetail {
        return UserDetail(
            id = entity.id,
            userId = entity.userId,
            fullName = entity.fullName,
            avatarUrl = entity.avatarUrl,
            phoneNumber = entity.phoneNumber,
            address = entity.address,
            bio = entity.bio,
            dateOfBirth = entity.dateOfBirth?.toLongOrNull(), // Convert String to Long (epoch millis)
            gender = Gender.fromString(entity.gender), // Convert String to Gender enum
        )
    }

    fun toEntity(domain: UserDetail): UserDetailEntity {
        return UserDetailEntity(
            id = domain.id,
            userId = domain.userId,
            fullName = domain.fullName,
            avatarUrl = domain.avatarUrl,
            phoneNumber = domain.phoneNumber,
            address = domain.address,
            bio = domain.bio,
            dateOfBirth = domain.dateOfBirth?.toString(), // Convert Long (epoch millis) to String
            gender = domain.gender?.name, // Convert Gender enum to String
        )
    }
}



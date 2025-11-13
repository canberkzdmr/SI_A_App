package com.cbo.core.data.mapper

import com.cbo.core.database.entity.UserWithDetail as UserWithDetailEntity
import com.cbo.core.domain.model.UserWithDetail
import javax.inject.Inject

class UserWithDetailEntityMapper @Inject constructor(
    private val userMapper: UserEntityMapper,
    private val userDetailMapper: UserDetailEntityMapper,
    private val userSettingsMapper: UserSettingsEntityMapper,
) {

    fun toDomain(entity: UserWithDetailEntity): UserWithDetail {
        return UserWithDetail(
            user = userMapper.toDomain(entity.user),
            userDetail = entity.userDetail?.let { userDetailMapper.toDomain(it) },
            userSettings = userSettingsMapper.toDomain(entity.userSettingsEntity),
        )
    }
}


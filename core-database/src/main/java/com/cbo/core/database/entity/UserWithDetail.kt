package com.cbo.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithDetail(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val userDetail: UserDetailEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val userSettingsEntity: UserSettingsEntity
)

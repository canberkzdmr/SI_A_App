package com.example.core.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithDetail(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val detail: UserDetailEntity?
)

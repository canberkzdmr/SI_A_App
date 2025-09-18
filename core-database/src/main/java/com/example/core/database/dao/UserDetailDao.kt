package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.core.database.entity.UserDetailEntity

@Dao
interface UserDetailDao : BaseDao<UserDetailEntity> {

    @Query("SELECT * FROM user_details WHERE userId = :userId")
    suspend fun getUserDetailByUserId(userId: Int): UserDetailEntity?
}

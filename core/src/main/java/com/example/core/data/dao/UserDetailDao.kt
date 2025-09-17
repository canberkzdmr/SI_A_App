package com.example.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.core.data.model.UserDetailEntity

@Dao
interface UserDetailDao : BaseDao<UserDetailEntity> {

    @Query("SELECT * FROM user_details WHERE userId = :userId")
    suspend fun getUserDetailByUserId(userId: Int): UserDetailEntity?
}
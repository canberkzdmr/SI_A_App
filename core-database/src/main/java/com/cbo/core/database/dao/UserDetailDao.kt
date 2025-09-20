package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cbo.core.database.entity.UserDetailEntity

@Dao
interface UserDetailDao : BaseDao<UserDetailEntity> {

    @Query("SELECT * FROM user_details WHERE userId = :userId")
    suspend fun getUserDetailByUserId(userId: Int): UserDetailEntity?
}

package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import com.example.core.database.entity.UserWithDetail
import javax.inject.Inject

class GetUserWithDetailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserWithDetail> {
        return repository.getUserWithDetail(userId)
    }
}
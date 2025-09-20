package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import com.cbo.core.database.entity.UserDetailEntity
import javax.inject.Inject

class UpsertUserDetailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(detail: UserDetailEntity): Result<Unit> {
        return repository.upsertUserDetail(detail)
    }
}
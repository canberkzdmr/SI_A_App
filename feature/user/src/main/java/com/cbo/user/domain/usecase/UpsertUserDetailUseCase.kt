package com.cbo.user.domain.usecase

import com.cbo.core.domain.model.UserDetail
import com.cbo.user.domain.repository.UserRepository
import javax.inject.Inject

class UpsertUserDetailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(detail: UserDetail): Result<Unit> {
        return repository.upsertUserDetail(detail)
    }
}
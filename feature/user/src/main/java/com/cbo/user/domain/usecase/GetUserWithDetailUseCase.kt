package com.cbo.user.domain.usecase

import com.cbo.core.domain.model.UserWithDetail
import com.cbo.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserWithDetailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: Int): Flow<UserWithDetail?> {
        return repository.getUserWithDetail(userId)
    }
}
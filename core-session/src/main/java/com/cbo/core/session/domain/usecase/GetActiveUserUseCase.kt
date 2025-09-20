package com.cbo.core.session.domain.usecase

import com.cbo.core.domain.model.User
import com.cbo.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveUserUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<User?> {
        return sessionRepository.activeUser
    }
}

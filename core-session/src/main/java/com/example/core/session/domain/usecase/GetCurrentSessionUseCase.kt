package com.example.core.session.domain.usecase

import com.example.core.session.domain.model.Session
import com.example.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Session?> {
        return sessionRepository.currentSession
    }
}

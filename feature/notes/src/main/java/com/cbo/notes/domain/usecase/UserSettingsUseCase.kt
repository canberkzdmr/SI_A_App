package com.cbo.notes.domain.usecase

import com.cbo.core.domain.model.ViewMode
import com.cbo.core.session.UserSession
import com.cbo.notes.data.repository.UserSettingsRepositoryImpl
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetNotesViewModeUseCase
    @Inject
    constructor(
        private val userSession: UserSession,
        private val userSettingsRepositoryImpl: UserSettingsRepositoryImpl,
    ) {
        suspend operator fun invoke(): Result<ViewMode> {
            val user =
                userSession.currentUser.first()
                    ?: return Result.failure(Throwable("Could not get user from session"))

            return userSettingsRepositoryImpl.getNotesViewMode(user.id).fold(
                onSuccess = { Result.success(it) },
                onFailure = { exception ->
                    Result.failure(Throwable(exception.message ?: "Error while getting 'Notes View Mode' Setting"))
                },
            )
        }
    }

class SetNotesViewModeUseCase
    @Inject
    constructor(
        private val userSession: UserSession,
        private val userSettingsRepositoryImpl: UserSettingsRepositoryImpl,
    ) {
        suspend operator fun invoke(viewMode: ViewMode): Result<Unit> {
            val user =
                userSession.currentUser.first()
                    ?: return Result.failure(Throwable("Could not get user from session"))

            return userSettingsRepositoryImpl.setNotesViewMode(user.id, viewMode)
        }
    }

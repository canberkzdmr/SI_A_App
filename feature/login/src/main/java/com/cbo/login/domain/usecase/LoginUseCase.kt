package com.cbo.login.domain.usecase

import android.util.Log
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.domain.model.User
import com.cbo.core.domain.usecase.GetUserSettingsUseCase
import com.cbo.core.domain.usecase.SetFirstLoginDoneUseCase
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.session.domain.repository.SessionRepository
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val getUserEntityUseCase: GetUserEntityUseCase,
        private val verifyPasswordUseCase: VerifyPasswordUseCase,
        private val getUserSettingsUseCase: GetUserSettingsUseCase,
        private val setFirstLoginDoneUseCase: SetFirstLoginDoneUseCase,
        private val sessionRepository: SessionRepository,
        private val userEntityMapper: UserEntityMapper,
    ) {
        suspend operator fun invoke(
            username: String,
            password: String,
        ): Result<User> {
            return try {
                getUserEntityUseCase.invoke(username).fold(
                    onSuccess = { user ->
                        val isValid = verifyPasswordUseCase(username, password)
                        if (!isValid) return Result.failure(LoginException.InvalidCredentialsException())

                        val userSettings = getUserSettingsUseCase.invoke(user.id)
                        userSettings.getOrNull()?.let {
                            if (!it.isFirstLoginDone) {
                                setFirstLoginDoneUseCase.invoke(
                                    user.id,
                                    true
                                )
                                return Result.failure(LoginException.FirstLoginIsNotCompleted())
                            }
                        } ?: run {
                            Log.e("LoginUseCase", "Could not get user settings")
                        }

                        val user = userEntityMapper.toDomain(user)
                        sessionRepository.setActiveUser(userEntityMapper.toEntity(user, byteArrayOf(), byteArrayOf(), "", "")) // mark as active
                        Result.success(user)
                    },
                    onFailure = {
                        return Result.failure(LoginException.UserNotFoundException())
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginUseCase", "Unknown error ${e.message}")
                return Result.failure(LoginException.UnknownException())
            }
        }
    }

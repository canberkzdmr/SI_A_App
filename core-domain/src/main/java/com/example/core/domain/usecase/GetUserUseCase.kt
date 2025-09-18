package com.example.core.domain.usecase

import com.example.core.domain.model.User
import com.example.core.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // TODO: Add implementation based on business requirements
    // This could get user by ID, username, or other criteria
}

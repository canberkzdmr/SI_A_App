package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository

class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke() {

    }
}
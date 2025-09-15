package com.example.login.domain.usecase

import com.example.login.domain.model.User
import com.example.login.domain.repository.UserRepository

class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(username: String): Result<User>? {
        return userRepository.getUser(username)
    }
}
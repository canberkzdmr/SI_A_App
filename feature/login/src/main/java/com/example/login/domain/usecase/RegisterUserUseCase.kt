package com.example.login.domain.usecase

import com.example.login.domain.model.User
import com.example.login.domain.repository.UserRepository

class RegisterUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): Boolean {
        return repository.registerUser(user)
    }
}
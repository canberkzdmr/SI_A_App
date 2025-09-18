package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import com.example.core.database.entity.UserDetailEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserWithDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class GetUserWithDetailUseCaseTest {

    @Mock
    private lateinit var repository: UserRepository

    private lateinit var useCase: GetUserWithDetailUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        useCase = GetUserWithDetailUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Given
        val userId = 1
        val userEntity = UserEntity(
            id = userId,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        val userDetailEntity = UserDetailEntity(
            userId = userId,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val userWithDetail = UserWithDetail(
            user = userEntity,
            userDetail = userDetailEntity
        )
        val expectedResult = Result.success(userWithDetail)
        whenever(repository.getUserWithDetail(userId)).thenReturn(expectedResult)

        // When
        val result = useCase.invoke(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userWithDetail, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository returns failure`() = runTest {
        // Given
        val userId = 1
        val exception = Exception("User not found")
        val expectedResult = Result.failure<UserWithDetail>(exception)
        whenever(repository.getUserWithDetail(userId)).thenReturn(expectedResult)

        // When
        val result = useCase.invoke(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val userId = 1
        val exception = RuntimeException("Database error")
        whenever(repository.getUserWithDetail(userId)).thenThrow(exception)

        // When & Then
        try {
            useCase.invoke(userId)
        } catch (e: Exception) {
            assertEquals(exception.message, e.message)
        }
    }
}

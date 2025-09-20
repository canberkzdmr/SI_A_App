package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import com.cbo.core.database.entity.UserDetailEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class UpsertUserDetailUseCaseTest {

    @Mock
    private lateinit var repository: UserRepository

    private lateinit var useCase: UpsertUserDetailUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        useCase = UpsertUserDetailUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val expectedResult = Result.success(Unit)
        whenever(repository.upsertUserDetail(userDetail)).thenReturn(expectedResult)

        // When
        val result = useCase.invoke(userDetail)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository returns failure`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val exception = Exception("Database error")
        val expectedResult = Result.failure<Unit>(exception)
        whenever(repository.upsertUserDetail(userDetail)).thenReturn(expectedResult)

        // When
        val result = useCase.invoke(userDetail)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should handle null user detail fields correctly`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = null,
            avatarUrl = null,
            bio = null,
            phoneNumber = null,
            address = null,
            dateOfBirth = null,
            gender = null
        )
        val expectedResult = Result.success(Unit)
        whenever(repository.upsertUserDetail(userDetail)).thenReturn(expectedResult)

        // When
        val result = useCase.invoke(userDetail)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val exception = RuntimeException("Database connection failed")
        whenever(repository.upsertUserDetail(userDetail)).thenThrow(exception)

        // When & Then
        try {
            useCase.invoke(userDetail)
        } catch (e: Exception) {
            assertEquals(exception.message, e.message)
        }
    }
}

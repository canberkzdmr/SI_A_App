package com.cbo.user.domain.usecase

import com.cbo.core.domain.model.User
import com.cbo.core.domain.model.UserDetail
import com.cbo.core.domain.model.UserSettings
import com.cbo.core.domain.model.UserWithDetail
import com.cbo.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `invoke should return user with detail from repository flow`() = runTest {
        val userId = 1
        val user = User(id = userId, username = "testuser", email = "test@example.com")
        val userDetail = UserDetail(
            id = 1,
            userId = userId,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val userSettings = UserSettings(userId = userId, isBiometricsEnabled = false, preferredLanguage = "tr")
        val userWithDetail = UserWithDetail(user = user, userDetail = userDetail, userSettings = userSettings)

        whenever(repository.getUserWithDetail(userId)).thenReturn(flowOf(userWithDetail))

        val result = useCase(userId).first()
        assertEquals(userWithDetail, result)
    }

    @Test
    fun `invoke should return null when repository emits null`() = runTest {
        val userId = 1
        whenever(repository.getUserWithDetail(userId)).thenReturn(flowOf(null))

        val result = useCase(userId).first()
        assertNull(result)
    }
}

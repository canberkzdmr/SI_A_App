package com.cbo.user.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.user.domain.usecase.UpsertUserDetailUseCase
import com.example.core.database.entity.UserDetailEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserWithDetail
import com.example.core.domain.model.User
import com.example.core.session.domain.usecase.GetActiveUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    @Mock
    private lateinit var getUserWithDetailUseCase: GetUserWithDetailUseCase

    @Mock
    private lateinit var upsertUserDetailUseCase: UpsertUserDetailUseCase

    private lateinit var viewModel: EditProfileViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = EditProfileViewModel(
            getActiveUserUseCase,
            getUserWithDetailUseCase,
            upsertUserDetailUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() {
        // Given - ViewModel is initialized in setUp()
        
        // When
        val uiState = viewModel.uiState.value
        
        // Then
        assertTrue(uiState.isLoading)
        assertEquals(-1, uiState.userId)
        assertEquals("", uiState.username)
        assertEquals("", uiState.email)
        assertEquals("", uiState.fullName)
        assertEquals("", uiState.avatarUrl)
        assertEquals("", uiState.bio)
        assertEquals("", uiState.phoneNumber)
        assertEquals("", uiState.address)
        assertNull(uiState.error)
        assertFalse(uiState.isSaved)
    }

    @Test
    fun `loadUser should update state when user exists and has details`() = runTest {
        // Given
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        val userEntity = UserEntity(
            id = 1,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        val userDetailEntity = UserDetailEntity(
            userId = 1,
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
        
        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(Result.success(userWithDetail))
        
        // When
        viewModel.loadUser()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.userId)
        assertEquals("testuser", uiState.username)
        assertEquals("test@example.com", uiState.email)
        assertEquals("Test User", uiState.fullName)
        assertEquals("avatar.jpg", uiState.avatarUrl)
        assertEquals("Test bio", uiState.bio)
        assertEquals("1234567890", uiState.phoneNumber)
        assertEquals("Test address", uiState.address)
        assertNull(uiState.error)
        assertFalse(uiState.isSaved)
        
        verify(getActiveUserUseCase)()
        verify(getUserWithDetailUseCase)(1)
    }

    @Test
    fun `loadUser should update state when user exists but has no details`() = runTest {
        // Given
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        val userEntity = UserEntity(
            id = 1,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        val userWithDetail = UserWithDetail(
            user = userEntity,
            userDetail = null
        )
        
        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(Result.success(userWithDetail))
        
        // When
        viewModel.loadUser()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.userId)
        assertEquals("testuser", uiState.username)
        assertEquals("test@example.com", uiState.email)
        assertEquals("", uiState.fullName) // Should be empty when userDetail is null
        assertEquals("", uiState.avatarUrl)
        assertEquals("", uiState.bio)
        assertEquals("", uiState.phoneNumber)
        assertEquals("", uiState.address)
        assertNull(uiState.error)
        assertFalse(uiState.isSaved)
    }

    @Test
    fun `loadUser should handle getUserWithDetail failure`() = runTest {
        // Given
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        val exception = Exception("User detail not found")
        
        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(Result.failure(exception))
        
        // When
        viewModel.loadUser()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("User detail not found", uiState.error)
    }

    @Test
    fun `loadUser should handle getActiveUserUseCase exception`() = runTest {
        // Given
        val exception = RuntimeException("Session error")
        whenever(getActiveUserUseCase()).thenThrow(exception)
        
        // When
        viewModel.loadUser()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("User informations could not retrieved", uiState.error)
    }

    @Test
    fun `updateFullName should update state`() {
        // Given
        val newFullName = "New Full Name"
        
        // When
        viewModel.updateFullName(newFullName)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newFullName, uiState.fullName)
    }

    @Test
    fun `updateAvatarUrl should update state`() {
        // Given
        val newAvatarUrl = "new_avatar.jpg"
        
        // When
        viewModel.updateAvatarUrl(newAvatarUrl)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newAvatarUrl, uiState.avatarUrl)
    }

    @Test
    fun `updateBio should update state`() {
        // Given
        val newBio = "New bio"
        
        // When
        viewModel.updateBio(newBio)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newBio, uiState.bio)
    }

    @Test
    fun `updatePhoneNumber should update state`() {
        // Given
        val newPhoneNumber = "9876543210"
        
        // When
        viewModel.updatePhoneNumber(newPhoneNumber)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newPhoneNumber, uiState.phoneNumber)
    }

    @Test
    fun `updateAddress should update state`() {
        // Given
        val newAddress = "New Address"
        
        // When
        viewModel.updateAddress(newAddress)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newAddress, uiState.address)
    }

    @Test
    fun `save should succeed when upsertUserDetailUseCase succeeds`() = runTest {
        // Given
        // First set up the state with some data
        viewModel.updateFullName("Test User")
        viewModel.updateBio("Test bio")
        viewModel.updatePhoneNumber("1234567890")
        viewModel.updateAddress("Test address")
        viewModel.updateAvatarUrl("avatar.jpg")
        
        whenever(upsertUserDetailUseCase.invoke(org.mockito.kotlin.any())).thenReturn(Result.success(Unit))
        
        // When
        viewModel.save()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isSaved)
        assertNull(uiState.error)
        
        verify(upsertUserDetailUseCase).invoke(org.mockito.kotlin.any())
    }

    @Test
    fun `save should handle failure when upsertUserDetailUseCase fails`() = runTest {
        // Given
        val exception = Exception("Save failed")
        whenever(upsertUserDetailUseCase.invoke(org.mockito.kotlin.any())).thenReturn(Result.failure(exception))
        
        // When
        viewModel.save()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isSaved)
        assertEquals("Save failed", uiState.error)
        
        verify(upsertUserDetailUseCase).invoke(org.mockito.kotlin.any())
    }

    @Test
    fun `save should set loading state during operation`() = runTest {
        // Given
        whenever(upsertUserDetailUseCase.invoke(org.mockito.kotlin.any())).thenReturn(Result.success(Unit))
        
        // When
        viewModel.save()
        
        // Then - immediately check loading state before advanceUntilIdle
        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isLoading)
        
        advanceUntilIdle()
        
        // After completion
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
    }
}

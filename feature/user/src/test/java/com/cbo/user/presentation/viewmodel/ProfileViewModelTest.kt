package com.cbo.user.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cbo.core.domain.model.User
import com.cbo.core.session.UserSession
import com.cbo.core.session.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var userSession: UserSession

    private lateinit var viewModel: ProfileViewModel

    private val mockCurrentUser = MutableStateFlow<User?>(null)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        whenever(userSession.currentUser).thenReturn(mockCurrentUser)
        
        viewModel = ProfileViewModel(logoutUseCase, userSession)
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
        assertEquals("", uiState.username)
        assertEquals("", uiState.email)
        assertEquals("", uiState.lastLoginDate)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `loadProfile should update state when user exists`() = runTest {
        // Given
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        
        // When
        mockCurrentUser.emit(testUser)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("testuser", uiState.username)
        assertEquals("test@example.com", uiState.email)
    }

    @Test
    fun `loadProfile should handle null user`() = runTest {
        // Given
        mockCurrentUser.emit(null)
        
        // When
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        // Note: Based on the implementation, when user is null, it doesn't update the state
        // so it remains as the initial state
        assertTrue(uiState.isLoading) // or check the actual behavior
    }

    @Test
    fun `logout should clear user data and emit LoggedOut event`() = runTest {
        // Given
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        mockCurrentUser.emit(testUser)
        advanceUntilIdle()
        
        // When
        viewModel.logout()
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.username)
        assertNull(uiState.email)
        assertFalse(uiState.isLoading)
        
        verify(logoutUseCase).invoke()
        verify(userSession).clearUser()
    }

    @Test
    fun `editProfile should update UI state`() {
        // Given
        val newUsername = "newusername"
        val newEmail = "newemail@example.com"
        
        // When
        viewModel.editProfile(newUsername, newEmail)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newUsername, uiState.username)
        assertEquals(newEmail, uiState.email)
    }

    @Test
    fun `changePassword should execute without throwing exception`() {
        // When & Then
        viewModel.changePassword() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `deleteAccount should set loading state`() {
        // When
        viewModel.deleteAccount()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertEquals("", uiState.username)
        assertEquals("", uiState.email)
        assertEquals("", uiState.lastLoginDate)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `themeChange should execute without throwing exception`() {
        // When & Then
        viewModel.themeChange() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `languageChange should execute without throwing exception`() {
        // When & Then
        viewModel.languageChange() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `manageCategories should execute without throwing exception`() {
        // When & Then
        viewModel.manageCategories() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `exportNotes should execute without throwing exception`() {
        // When & Then
        viewModel.exportNotes() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `enableBiometrics should execute without throwing exception`() {
        // When & Then
        viewModel.enableBiometrics() // Method is currently empty, just ensure it doesn't throw
    }

    @Test
    fun `contactSupport should execute without throwing exception`() {
        // When & Then
        viewModel.contactSupport() // Method is currently empty, just ensure it doesn't throw
    }
}

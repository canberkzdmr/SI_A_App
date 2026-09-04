package com.cbo.user.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cbo.core.domain.model.User
import com.cbo.core.domain.model.UserDetail
import com.cbo.core.domain.model.UserSettings
import com.cbo.core.domain.model.UserWithDetail
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.user.domain.usecase.SaveImageUseCase
import com.cbo.user.domain.usecase.UpsertUserDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
import org.mockito.kotlin.any
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

    @Mock
    private lateinit var saveImageUseCase: SaveImageUseCase

    private lateinit var viewModel: EditProfileViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = EditProfileViewModel(
            getActiveUserUseCase = getActiveUserUseCase,
            getUserWithDetailUseCase = getUserWithDetailUseCase,
            upsertUserDetailUseCase = upsertUserDetailUseCase,
            saveImageUseCase = saveImageUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() {
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertEquals(-1, uiState.userId)
        assertEquals("", uiState.username)
        assertEquals("", uiState.email)
        assertEquals("", uiState.fullName)
    }

    @Test
    fun `loadUser should update state when user exists`() = runTest {
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")
        val userDetail = UserDetail(
            id = 10,
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
            user = testUser,
            userDetail = userDetail,
            userSettings = UserSettings(userId = 1, isBiometricsEnabled = false, preferredLanguage = "tr")
        )

        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(flowOf(userWithDetail))

        viewModel.loadUser()
        advanceUntilIdle()

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
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")
        val userWithDetail = UserWithDetail(
            user = testUser,
            userDetail = null,
            userSettings = UserSettings(userId = 1, isBiometricsEnabled = false, preferredLanguage = "tr")
        )

        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(flowOf(userWithDetail))

        viewModel.loadUser()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.userId)
        assertEquals("testuser", uiState.username)
        assertEquals("test@example.com", uiState.email)
        assertEquals("", uiState.fullName)
        assertEquals("", uiState.avatarUrl)
        assertEquals("", uiState.bio)
        assertEquals("", uiState.phoneNumber)
        assertEquals("", uiState.address)
        assertNull(uiState.error)
        assertFalse(uiState.isSaved)
    }

    @Test
    fun `loadUser should handle getUserWithDetail failure`() = runTest {
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")

        whenever(getActiveUserUseCase()).thenReturn(flowOf(testUser))
        whenever(getUserWithDetailUseCase(1)).thenReturn(flow { throw RuntimeException("User detail not found") })

        viewModel.loadUser()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("User detail not found", uiState.error)
    }

    @Test
    fun `loadUser should handle getActiveUserUseCase exception`() = runTest {
        whenever(getActiveUserUseCase()).thenReturn(flow { throw RuntimeException("Session error") })

        viewModel.loadUser()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("User informations could not retrieved", uiState.error)
    }

    @Test
    fun `updateFullName should update state`() {
        val newFullName = "New Full Name"
        viewModel.updateFullName(newFullName)
        val uiState = viewModel.uiState.value
        assertEquals(newFullName, uiState.fullName)
    }

    @Test
    fun `updateAvatarUrl should update state`() {
        val newAvatarUrl = "new_avatar.jpg"
        viewModel.updateAvatarUrl(newAvatarUrl)
        val uiState = viewModel.uiState.value
        assertEquals(newAvatarUrl, uiState.avatarUrl)
    }

    @Test
    fun `updateBio should update state`() {
        val newBio = "New bio"
        viewModel.updateBio(newBio)
        val uiState = viewModel.uiState.value
        assertEquals(newBio, uiState.bio)
    }

    @Test
    fun `updatePhoneNumber should update state`() {
        val newPhoneNumber = "9876543210"
        viewModel.updatePhoneNumber(newPhoneNumber)
        val uiState = viewModel.uiState.value
        assertEquals(newPhoneNumber, uiState.phoneNumber)
    }

    @Test
    fun `updateAddress should update state`() {
        val newAddress = "New Address"
        viewModel.updateAddress(newAddress)
        val uiState = viewModel.uiState.value
        assertEquals(newAddress, uiState.address)
    }

    @Test
    fun `save should succeed when upsertUserDetailUseCase succeeds`() = runTest {
        viewModel.updateFullName("Test User")
        viewModel.updateBio("Test bio")
        viewModel.updatePhoneNumber("1234567890")
        viewModel.updateAddress("Test address")
        viewModel.updateAvatarUrl("avatar.jpg")

        whenever(upsertUserDetailUseCase.invoke(any())).thenReturn(Result.success(Unit))

        viewModel.save()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isSaved)
        assertNull(uiState.error)

        verify(upsertUserDetailUseCase).invoke(any())
    }

    @Test
    fun `save should handle failure when upsertUserDetailUseCase fails`() = runTest {
        val exception = Exception("Save failed")
        whenever(upsertUserDetailUseCase.invoke(any())).thenReturn(Result.failure(exception))

        viewModel.save()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isSaved)
        assertEquals("Save failed", uiState.error)

        verify(upsertUserDetailUseCase).invoke(any())
    }

    @Test
    fun `save should set loading state during operation`() = runTest {
        whenever(upsertUserDetailUseCase.invoke(any())).thenReturn(Result.success(Unit))

        viewModel.save()

        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isLoading)

        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
    }
}

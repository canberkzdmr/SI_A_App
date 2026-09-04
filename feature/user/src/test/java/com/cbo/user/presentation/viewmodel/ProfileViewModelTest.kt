package com.cbo.user.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cbo.core.domain.model.RestoreSummary
import com.cbo.core.domain.model.User
import com.cbo.core.domain.usecase.ExportBackupUseCase
import com.cbo.core.domain.usecase.RestoreBackupUseCase
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.domain.usecase.theme.ToggleDarkThemeUseCase
import com.cbo.core.session.UserSession
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.core.session.domain.usecase.LogoutUseCase
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var userSession: UserSession

    @Mock
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    @Mock
    private lateinit var getUserWithDetailUseCase: GetUserWithDetailUseCase

    @Mock
    private lateinit var setBiometricEnabledUseCase: SetBiometricEnabledUseCase

    @Mock
    private lateinit var toggleDarkThemeUseCase: ToggleDarkThemeUseCase

    @Mock
    private lateinit var exportBackupUseCase: ExportBackupUseCase

    @Mock
    private lateinit var restoreBackupUseCase: RestoreBackupUseCase

    private lateinit var viewModel: ProfileViewModel

    private val mockCurrentUser = MutableStateFlow<User?>(null)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(userSession.currentUser).thenReturn(mockCurrentUser)
        whenever(getActiveUserUseCase()).thenReturn(mockCurrentUser)

        viewModel = ProfileViewModel(
            logoutUseCase = logoutUseCase,
            userSession = userSession,
            getActiveUserUseCase = getActiveUserUseCase,
            getUserWithDetailUseCase = getUserWithDetailUseCase,
            setBiometricEnabledUseCase = setBiometricEnabledUseCase,
            toggleDarkThemeUseCase = toggleDarkThemeUseCase,
            exportBackupUseCase = exportBackupUseCase,
            restoreBackupUseCase = restoreBackupUseCase,
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
        assertEquals("", uiState.username)
        assertEquals("", uiState.email)
        assertEquals("", uiState.lastLoginDate)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `logout should clear user data and emit LoggedOut event`() = runTest {
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")
        mockCurrentUser.emit(testUser)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.username)
        assertNull(uiState.email)
        assertFalse(uiState.isLoading)

        verify(logoutUseCase).invoke()
        verify(userSession).clearUser()
    }

    @Test
    fun `editProfile should update UI state`() {
        val newUsername = "newusername"
        val newEmail = "newemail@example.com"

        viewModel.editProfile(newUsername, newEmail)

        val uiState = viewModel.uiState.value
        assertEquals(newUsername, uiState.username)
        assertEquals(newEmail, uiState.email)
    }

    @Test
    fun `deleteAccount should set loading state`() {
        viewModel.deleteAccount()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
    }

    @Test
    fun `exportBackup should invoke exportBackupUseCase when userId is valid`() = runTest {
        // Set user id in state
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")
        whenever(getUserWithDetailUseCase(1)).thenReturn(
            flowOf(
                com.cbo.core.domain.model.UserWithDetail(
                    user = testUser,
                    userDetail = null,
                    userSettings = com.cbo.core.domain.model.UserSettings(
                        userId = 1,
                        isBiometricsEnabled = false,
                        preferredLanguage = "tr"
                    )
                )
            )
        )
        mockCurrentUser.emit(testUser)
        advanceUntilIdle()

        val fakeJson = """{"version":1}"""
        whenever(exportBackupUseCase.invoke(1)).thenReturn(fakeJson)

        var emittedJson: String? = null
        viewModel.exportBackup { json ->
            emittedJson = json
        }
        advanceUntilIdle()

        assertEquals(fakeJson, emittedJson)
        verify(exportBackupUseCase).invoke(1)
    }

    @Test
    fun `restoreBackup should invoke restoreBackupUseCase with correct params`() = runTest {
        val testUser = User(id = 1, username = "testuser", email = "test@example.com")
        whenever(getUserWithDetailUseCase(1)).thenReturn(
            flowOf(
                com.cbo.core.domain.model.UserWithDetail(
                    user = testUser,
                    userDetail = null,
                    userSettings = com.cbo.core.domain.model.UserSettings(
                        userId = 1,
                        isBiometricsEnabled = false,
                        preferredLanguage = "tr"
                    )
                )
            )
        )
        mockCurrentUser.emit(testUser)
        advanceUntilIdle()

        val fakeJson = """{"version":1}"""
        val fakeSummary = RestoreSummary(1, 1, 1, 0, 0)
        whenever(restoreBackupUseCase.invoke(1, fakeJson)).thenReturn(fakeSummary)

        viewModel.restoreBackup(fakeJson)
        advanceUntilIdle()

        verify(restoreBackupUseCase).invoke(1, fakeJson)
    }
}

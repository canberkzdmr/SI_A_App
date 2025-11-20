package com.cbo.notes.presentation.viewmodel

import com.cbo.core.domain.model.User
import com.cbo.core.domain.model.ViewMode
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.*
import com.cbo.ui.snackbar.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userSession: UserSession = mock()
    private val getNotesUseCase: GetNotesUseCase = mock()
    private val getFavoriteNotesUseCase: GetFavoriteNotesUseCase = mock()
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase = mock()
    private val searchNotesUseCase: SearchNotesUseCase = mock()
    private val getCategoriesUseCase: GetCategoriesUseCase = mock()
    private val getTagsUseCase: GetTagsUseCase = mock()
    private val toggleNotePinnedUseCase: ToggleNotePinnedUseCase = mock()
    private val toggleNoteFavoriteUseCase: ToggleNoteFavoriteUseCase = mock()
    private val archiveNoteUseCase: ArchiveNoteUseCase = mock()
    private val deleteNoteUseCase: DeleteNoteUseCase = mock()
    private val getNotesViewModeUseCase: GetNotesViewModeUseCase = mock()
    private val setNotesViewModeUseCase: SetNotesViewModeUseCase = mock()
    private val snackbarManager: SnackbarManager = mock()

    private lateinit var viewModel: NotesViewModel

    private val dummyUser = User(1, "test", "test@test.com", "hashedpass")
    
    private val dummyCategories = listOf(
        Category(id = 1, userId = 1, name = "Work", color = "#FF0000")
    )
    
    private val dummyTags = listOf(
        Tag(id = 1, userId = 1, name = "Important", color = "#00FF00")
    )

    private val dummyNotes = listOf(
        Note(
            id = 1, 
            userId = 1, 
            title = "Title 1", 
            content = "Content 1",
            category = dummyCategories[0],
            tags = dummyTags
        ),
        Note(
            id = 2, 
            userId = 1, 
            title = "Title 2", 
            content = "Content 2"
        )
    )

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)

        whenever(userSession.currentUser).thenReturn(flowOf(dummyUser))
        whenever(getNotesUseCase(1)).thenReturn(flowOf(dummyNotes))
        whenever(getCategoriesUseCase(1)).thenReturn(flowOf(dummyCategories))
        whenever(getTagsUseCase(1)).thenReturn(flowOf(dummyTags))
        whenever(getNotesViewModeUseCase()).thenReturn(Result.success(ViewMode.LIST))

        viewModel = NotesViewModel(
            userSession,
            getNotesUseCase,
            getFavoriteNotesUseCase,
            getArchivedNotesUseCase,
            searchNotesUseCase,
            getCategoriesUseCase,
            getTagsUseCase,
            toggleNotePinnedUseCase,
            toggleNoteFavoriteUseCase,
            archiveNoteUseCase,
            deleteNoteUseCase,
            getNotesViewModeUseCase,
            setNotesViewModeUseCase,
            snackbarManager
        )
        
        // Set test dispatcher for background operations
        viewModel.defaultDispatcher = testDispatcher
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads data successfully`() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.uiState.collect {}
        }
        
        val loadedState = viewModel.uiState.value
        assertEquals(false, loadedState.isLoading)
        assertEquals(dummyNotes, loadedState.notes)
        assertEquals(dummyCategories, loadedState.categories)
        assertEquals(dummyTags, loadedState.tags)
        assertEquals(ViewMode.LIST, loadedState.viewMode)
        
        collectJob.cancel()
    }

    @Test
    fun `searchNotes filters notes`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect {} }
        
        viewModel.searchNotes("Title 1")
        
        val filteredState = viewModel.uiState.value
        assertEquals(1, filteredState.filteredNotes.size)
        assertEquals("Title 1", filteredState.filteredNotes[0].title)

        collectJob.cancel()
    }
}

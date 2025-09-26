package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.ViewMode
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val userSession: UserSession,
    private val getTagsUseCase: GetTagsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isLoading = true) }

                getTagsUseCase(user.id)
                    .catch { throwable ->
                        SnackbarManager.showMessage(SnackbarMessage.Error("Failed to load tags"))
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load tags: ${throwable.message}"
                            )
                        }
                    }
                    .collect { tags ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                tags = tags,
                            )
                        }
                    }
            }
        }
    }

    fun showCreateTagDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                editingTag = null,
                dialogTagName = "",
            )
        }
    }

    fun showEditTagDialog(tag: Tag) {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                editingTag = tag,
                dialogTagName = tag.name,
            )
        }
    }
}

data class TagsUiState(
    val isLoading: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val name: String = "",
    val color: String? = null,
    val usageCount: Int = 0,
    val selectedTags: List<Tag> = emptyList(),
    val showCreateDialog: Boolean = false,
    val editingTag: Tag? = null,
    val dialogTagName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOrder: SortOrder = SortOrder.UPDATED_ASC,
    val errorMessage: String? = null
)
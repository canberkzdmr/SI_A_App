package com.cbo.notes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

                val tags = getTagsUseCase(user.id).first()

            }
        }
    }
}

data class TagsUiState(
    val isLoading: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val name: String = "",
    val color: String? = null,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOrder: SortOrder = SortOrder.UPDATED_ASC,
    val errorMessage: String? = null
)
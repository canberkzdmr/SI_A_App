package com.cbo.statistics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.notes.domain.model.NoteStatistics
import com.cbo.notes.domain.usecase.GetNoteStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.cbo.statistics.R

/**
 * ViewModel for the Statistics screen.
 * Collects [NoteStatistics] from [GetNoteStatisticsUseCase] and exposes
 * a [StatisticsUiState] to the UI layer.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetNoteStatisticsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, errorRes = null) }
            getStatisticsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message,
                            errorRes = if (e.message == null) R.string.unknown_error else null
                        )
                    }
                }
                .collect { stats ->
                    _uiState.update {
                        it.copy(isLoading = false, statistics = stats, error = null, errorRes = null)
                    }
                }
        }
    }

    fun retry() = loadStatistics()
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: NoteStatistics? = null,
    val error: String? = null,
    val errorRes: Int? = null,
)

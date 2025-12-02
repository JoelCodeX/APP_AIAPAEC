package com.jotadev.aiapaec.ui.screens.exams.answers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamResult(
    val id: String,
    val date: String, // Formato YYYY-MM-DD
    val score: Float // 0..100
)

data class ResultsUiState(
    val results: List<ExamResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ResultsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val sample = listOf(
                    ExamResult("1", "2024-01-15", 78f),
                    ExamResult("2", "2024-02-02", 84f),
                    ExamResult("3", "2024-02-20", 65f),
                    ExamResult("4", "2024-03-11", 91f),
                    ExamResult("5", "2024-04-05", 73f),
                    ExamResult("6", "2024-05-19", 88f),
                    ExamResult("7", "2024-05-27", 70f),
                    ExamResult("8", "2024-06-09", 82f)
                )
                _uiState.value = _uiState.value.copy(results = sample, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun refresh() {
        loadResults()
    }
}
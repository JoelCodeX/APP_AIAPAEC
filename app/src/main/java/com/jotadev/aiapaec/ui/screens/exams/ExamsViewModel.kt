package com.jotadev.aiapaec.ui.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Exam(
    val id: String,
    val title: String,
    val subject: String,
    val date: String,
    val questionsCount: Int,
    val status: ExamStatus
)

enum class ExamStatus {
    DRAFT, ACTIVE, COMPLETED, ARCHIVED
}

data class ExamsUiState(
    val isLoading: Boolean = false,
    val exams: List<Exam> = emptyList(),
    val selectedFilter: ExamStatus? = null,
    val searchQuery: String = "",
    val error: String? = null
)

class ExamsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()
    
    init {
        loadExams()
    }
    
    private fun loadExams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Simular carga de exámenes
                val exams = listOf(
                    Exam("1", "Examen de Matemáticas", "Matemáticas", "2024-01-15", 20, ExamStatus.ACTIVE),
                    Exam("2", "Evaluación de Historia", "Historia", "2024-01-10", 15, ExamStatus.COMPLETED),
                    Exam("3", "Quiz de Ciencias", "Ciencias", "2024-01-20", 10, ExamStatus.DRAFT),
                    Exam("4", "Examen Final Inglés", "Inglés", "2024-01-05", 25, ExamStatus.ARCHIVED)
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exams = exams
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar los exámenes: ${e.message}"
                )
            }
        }
    }
    
    fun filterExams(status: ExamStatus?) {
        _uiState.value = _uiState.value.copy(selectedFilter = status)
    }
    
    fun searchExams(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun deleteExam(examId: String) {
        viewModelScope.launch {
            val currentExams = _uiState.value.exams
            val updatedExams = currentExams.filter { it.id != examId }
            _uiState.value = _uiState.value.copy(exams = updatedExams)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshExams() {
        loadExams()
    }
}
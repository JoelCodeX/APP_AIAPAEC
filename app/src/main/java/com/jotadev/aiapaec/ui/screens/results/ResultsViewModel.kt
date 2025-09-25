package com.jotadev.aiapaec.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamResult(
    val id: String,
    val examTitle: String,
    val studentName: String,
    val score: Float,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val date: String,
    val grade: String
)

data class ResultsStatistics(
    val totalExams: Int,
    val averageScore: Float,
    val highestScore: Float,
    val lowestScore: Float,
    val passRate: Float
)

data class ResultsUiState(
    val isLoading: Boolean = false,
    val results: List<ExamResult> = emptyList(),
    val statistics: ResultsStatistics? = null,
    val selectedPeriod: String = "Último mes",
    val searchQuery: String = "",
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
                // Simular carga de resultados
                val results = listOf(
                    ExamResult("1", "Examen de Matemáticas", "Juan Pérez", 85.5f, 20, 17, "2024-01-15", "A"),
                    ExamResult("2", "Evaluación de Historia", "María García", 92.0f, 15, 14, "2024-01-14", "A+"),
                    ExamResult("3", "Quiz de Ciencias", "Carlos López", 78.0f, 10, 8, "2024-01-13", "B+"),
                    ExamResult("4", "Examen Final Inglés", "Ana Martínez", 95.5f, 25, 24, "2024-01-12", "A+"),
                    ExamResult("5", "Evaluación Física", "Luis Rodríguez", 68.0f, 20, 14, "2024-01-11", "C+")
                )
                
                val statistics = ResultsStatistics(
                    totalExams = results.size,
                    averageScore = results.map { it.score }.average().toFloat(),
                    highestScore = results.maxOf { it.score },
                    lowestScore = results.minOf { it.score },
                    passRate = (results.count { it.score >= 70 }.toFloat() / results.size) * 100
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = results,
                    statistics = statistics
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar los resultados: ${e.message}"
                )
            }
        }
    }
    
    fun filterByPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadResults() // Recargar con el nuevo filtro
    }
    
    fun searchResults(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun exportResults() {
        viewModelScope.launch {
            // Lógica para exportar resultados
            // Por ahora solo simular
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshResults() {
        loadResults()
    }
}
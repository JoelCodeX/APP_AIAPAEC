package com.jotadev.aiapaec.ui.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizAnswer
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnswersUiState(
    val quiz: Quiz? = null,
    val answers: List<QuizAnswer> = emptyList(),
    val pointsPerQuestion: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AnswersViewModel(
    private val quizzesRepository: QuizzesRepositoryImpl = QuizzesRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnswersUiState())
    val uiState: StateFlow<AnswersUiState> = _uiState

    fun load(examId: String) {
        val id = examId.toIntOrNull()
        if (id == null) {
            _uiState.update { it.copy(errorMessage = "ID de evaluación inválido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = quizzesRepository.getQuiz(id)) {
                is Result.Success -> {
                    val quiz = result.data
                    // Compute points with fallback later once answers are loaded if numQuestions is missing
                    val computedPoints = computePointsPerQuestion(quiz.numQuestions)
                    _uiState.update { it.copy(quiz = quiz, pointsPerQuestion = computedPoints) }
                    loadAnswers(quiz.id)
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun computePointsPerQuestion(numQuestions: Int?): Double {
        val n = (numQuestions ?: 0)
        if (n <= 0) return 0.0
        // Siempre evaluar sobre 100 puntos
        return 100.0 / n
    }

    private fun loadAnswers(quizId: Int) {
        viewModelScope.launch {
            when (val result = quizzesRepository.getQuizAnswers(quizId)) {
                is Result.Success -> {
                    // Sort answers by question number ascending
                    val items = result.data.items.sortedBy { it.questionNumber }
                    // If pointsPerQuestion is zero due to missing numQuestions, fallback to answers.size
                    val current = _uiState.value
                    val effectiveN = current.quiz?.numQuestions?.takeIf { it > 0 } ?: items.size
                    val points = if (effectiveN > 0) 100.0 / effectiveN else 0.0
                    _uiState.update { it.copy(answers = items, pointsPerQuestion = points) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Result.Loading -> {
                    // no-op
                }
            }
        }
    }
}
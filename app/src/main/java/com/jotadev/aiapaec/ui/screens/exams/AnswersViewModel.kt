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
    val errorMessage: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val lastSaveSucceeded: Boolean = false
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
                    _uiState.update { it.copy(quiz = quiz, pointsPerQuestion = computedPoints, hasUnsavedChanges = false, lastSaveSucceeded = false) }
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
                    val apiItems = result.data.items.sortedBy { it.questionNumber }
                    if (apiItems.isNotEmpty()) {
                        val current = _uiState.value
                        val effectiveN = current.quiz?.numQuestions?.takeIf { it > 0 } ?: apiItems.size
                        val points = if (effectiveN > 0) 100.0 / effectiveN else 0.0
                        _uiState.update { it.copy(answers = apiItems, pointsPerQuestion = points, hasUnsavedChanges = false, lastSaveSucceeded = false) }
                    } else {
                        // FALLBACK A parsed_keys DEL ÚLTIMO SOLUCIONARIO
                        when (val keysRes = quizzesRepository.listAnswerKeys(quizId, page = 1, pageSize = 10)) {
                            is Result.Success -> {
                                val keys = keysRes.data.items
                                val latest = keys.maxByOrNull { it.version }
                                val parsed = latest?.parsedKeys ?: emptyList()
                                val items = parsed.map { m ->
                                    val qn = (m["question_number"] as Number).toInt()
                                    val opt = m["correct_option"] as String
                                    val pts = (m["points_value"] as? Number)?.toDouble()
                                    QuizAnswer(
                                        id = 0,
                                        quizId = quizId,
                                        questionNumber = qn,
                                        correctOption = opt,
                                        pointsValue = pts,
                                        createdAt = null,
                                        updatedAt = null
                                    )
                                }.sortedBy { it.questionNumber }
                                val current = _uiState.value
                                val effectiveN = current.quiz?.numQuestions?.takeIf { it > 0 } ?: items.size
                                val points = if (effectiveN > 0) 100.0 / effectiveN else 0.0
                                _uiState.update { it.copy(answers = items, pointsPerQuestion = points, hasUnsavedChanges = false, lastSaveSucceeded = false) }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(errorMessage = keysRes.message) }
                            }
                            is Result.Loading -> {
                                // no-op
                            }
                        }
                    }
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

    fun deleteAnswerKey(examId: String) {
        val id = examId.toIntOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = quizzesRepository.deleteLatestAnswerKey(id)) {
                is Result.Success -> {
                    // Refresh quiz data and clear answers
                    when (val q = quizzesRepository.getQuiz(id)) {
                        is Result.Success -> _uiState.update { it.copy(quiz = q.data) }
                        else -> {}
                    }
                    _uiState.update { it.copy(answers = emptyList(), pointsPerQuestion = 0.0, isLoading = false, hasUnsavedChanges = false, lastSaveSucceeded = false) }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun updateAnswerOption(questionNumber: Int, option: String) {
        val updated = _uiState.value.answers.map {
            if (it.questionNumber == questionNumber) it.copy(correctOption = option) else it
        }
        _uiState.update { it.copy(answers = updated, hasUnsavedChanges = true) }
    }

    fun updateAnswerPoints(questionNumber: Int, points: Double?) {
        val updated = _uiState.value.answers.map {
            if (it.questionNumber == questionNumber) it.copy(pointsValue = points) else it
        }
        _uiState.update { it.copy(answers = updated, hasUnsavedChanges = true) }
    }

    fun saveAnswers(examId: String) {
        val id = examId.toIntOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val payload = _uiState.value.answers.map {
                com.jotadev.aiapaec.data.api.UpdateAnswerItem(
                    question_number = it.questionNumber,
                    correct_option = it.correctOption,
                    points_value = it.pointsValue
                )
            }
            when (val res = quizzesRepository.updateAnswerKeys(id, payload)) {
                is Result.Success -> {
                    loadAnswers(id)
                    _uiState.update { it.copy(isLoading = false, hasUnsavedChanges = false, lastSaveSucceeded = true) }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun applyUniformPoints() {
        val pts = _uiState.value.pointsPerQuestion
        val updated = _uiState.value.answers.map { it.copy(pointsValue = pts) }
        _uiState.update { it.copy(answers = updated, hasUnsavedChanges = true) }
    }

    fun ackSaveSuccess() {
        _uiState.update { it.copy(lastSaveSucceeded = false) }
    }
}
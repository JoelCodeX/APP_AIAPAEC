package com.jotadev.aiapaec.ui.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.usecases.CreateQuizUseCase
import com.jotadev.aiapaec.domain.usecases.DeleteQuizUseCase
import com.jotadev.aiapaec.domain.usecases.GetQuizzesUseCase
import com.jotadev.aiapaec.domain.usecases.UpdateQuizUseCase
import com.jotadev.aiapaec.ui.components.Exam as UiExam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamsUiState(
    val isLoading: Boolean = false,
    val exams: List<UiExam> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class ExamsViewModel : ViewModel() {
    private val quizzesRepository = QuizzesRepositoryImpl()
    private val getQuizzes = GetQuizzesUseCase(quizzesRepository)
    private val createQuiz = CreateQuizUseCase(quizzesRepository)
    private val updateQuiz = UpdateQuizUseCase(quizzesRepository)
    private val deleteQuiz = DeleteQuizUseCase(quizzesRepository)

    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()

    init {
        loadExams()
    }

    fun loadExams(page: Int = 1, perPage: Int = 20, query: String? = null, classId: Int? = null, bimesterId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = getQuizzes(page, perPage, query, classId, bimesterId)) {
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.Success -> {
                    val items = result.data.items.map { it.toUiExam() }
                    _uiState.value = _uiState.value.copy(isLoading = false, exams = items)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun searchExams(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun createExam(title: String, classId: Int, bimesterId: Int) {
        viewModelScope.launch {
            when (val result = createQuiz(
                title = title,
                description = null,
                classId = classId,
                bimesterId = bimesterId,
                totalPoints = null,
                numQuestions = null,
                pointsPerQuestion = null,
                answerKeyFile = null,
                keyVersion = null
            )) {
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.Success -> {
                    val created = result.data.toUiExam()
                    _uiState.value = _uiState.value.copy(exams = _uiState.value.exams + created)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun updateExam(id: Int, title: String, classId: Int, bimesterId: Int) {
        viewModelScope.launch {
            when (val result = updateQuiz(
                id = id,
                title = title,
                description = null,
                classId = classId,
                bimesterId = bimesterId,
                totalPoints = null,
                numQuestions = null,
                pointsPerQuestion = null,
                answerKeyFile = null,
                keyVersion = null
            )) {
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.Success -> {
                    val updated = result.data.toUiExam()
                    val list = _uiState.value.exams.map { if (it.id == updated.id) updated else it }
                    _uiState.value = _uiState.value.copy(exams = list)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun deleteExam(examId: String) {
        val idInt = examId.toIntOrNull() ?: return
        viewModelScope.launch {
            when (val result = deleteQuiz(idInt)) {
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(exams = _uiState.value.exams.filter { it.id != examId })
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshExams() {
        loadExams()
    }

    private fun Quiz.toUiExam(): UiExam {
        return UiExam(
            id = this.id.toString(),
            name = this.title,
            className = this.className ?: "",
            bimester = this.bimesterName ?: "",
            type = "Sin asignar",
            date = this.createdAt ?: "",
            isApplied = false,
            numQuestions = this.numQuestions
        )
    }
}
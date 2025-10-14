package com.jotadev.aiapaec.ui.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.usecases.GetStudentUseCase
import com.jotadev.aiapaec.domain.usecases.GetQuizzesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentDetailsUiState(
    val student: Student? = null,
    val exams: List<Quiz> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StudentDetailsViewModel(
    private val getStudent: GetStudentUseCase = GetStudentUseCase(StudentRepositoryImpl()),
    private val getQuizzes: GetQuizzesUseCase = GetQuizzesUseCase(QuizzesRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentDetailsUiState())
    val uiState: StateFlow<StudentDetailsUiState> = _uiState

    fun load(studentId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getStudent(studentId)) {
                is Result.Success -> {
                    val student = result.data
                    _uiState.update { it.copy(student = student) }
                    // Cargar exámenes por clase del estudiante si disponible
                    val classId = student.classId
                    if (classId != null) {
                        when (val examsResult = getQuizzes(page = 1, perPage = 50, query = null, classId = classId, bimesterId = null)) {
                            is Result.Success -> {
                                _uiState.update { it.copy(exams = examsResult.data.items, isLoading = false) }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(errorMessage = examsResult.message, isLoading = false) }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoading = true) }
                            }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
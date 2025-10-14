package com.jotadev.aiapaec.ui.screens.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClassStudentsUiState(
    val students: List<Student> = emptyList(),
    val page: Int = 1,
    val perPage: Int = 50,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ClassStudentsViewModel(
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassStudentsUiState())
    val uiState: StateFlow<ClassStudentsUiState> = _uiState

    private var currentClassId: Int? = null

    fun load(classId: Int) {
        currentClassId = classId
        fetchStudents(page = 1)
    }

    fun refresh() {
        fetchStudents(page = 1)
    }

    private fun fetchStudents(page: Int = _uiState.value.page, perPage: Int = _uiState.value.perPage) {
        val classId = currentClassId ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, page = page, perPage = perPage) }
        viewModelScope.launch {
            when (val result = getStudents(page = page, perPage = perPage, query = null)) {
                is Result.Success -> {
                    val filtered = result.data.items.filter { it.classId == classId }
                    _uiState.update { it.copy(students = filtered, isLoading = false) }
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
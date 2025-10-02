package com.jotadev.aiapaec.ui.screens.students

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

data class StudentsUiState(
    val students: List<Student> = emptyList(),
    val query: String = "",
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val pages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StudentsViewModel(
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState

    init {
        fetchStudents()
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun fetchStudents(page: Int? = null) {
        val targetPage = page ?: _uiState.value.page
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getStudents(targetPage, _uiState.value.perPage, _uiState.value.query.ifBlank { null })) {
                is Result.Success -> {
                    val pageData = result.data
                    _uiState.update {
                        it.copy(
                            students = pageData.items,
                            page = pageData.page,
                            perPage = pageData.perPage,
                            total = pageData.total,
                            pages = pageData.pages,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
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
}
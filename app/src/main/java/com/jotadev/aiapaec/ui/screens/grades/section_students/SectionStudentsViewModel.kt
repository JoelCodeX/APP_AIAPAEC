package com.jotadev.aiapaec.ui.screens.grades.section_students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SectionStudentsUiState(
    val students: List<Student> = emptyList(),
    val query: String = "",
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val pages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SectionStudentsViewModel(
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl())
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionStudentsUiState())
    val uiState: StateFlow<SectionStudentsUiState> = _uiState

    private var currentGradeId: Int? = null
    private var currentSectionId: Int? = null

    fun init(gradeId: Int, sectionId: Int) {
        if (currentGradeId == gradeId && currentSectionId == sectionId) return
        currentGradeId = gradeId
        currentSectionId = sectionId
        fetchStudents(page = 1)
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun fetchStudents(page: Int? = null) {
        val targetPage = page ?: _uiState.value.page
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = getStudents(
                targetPage,
                _uiState.value.perPage,
                _uiState.value.query.ifBlank { null },
                currentGradeId,
                currentSectionId
            )) {
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

    fun refresh() {
        fetchStudents(page = 1)
    }
}

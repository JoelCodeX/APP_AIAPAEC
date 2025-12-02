package com.jotadev.aiapaec.ui.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlin.collections.emptyList

data class StudentsUiState(
    val students: List<Student> = emptyList(),
    val query: String = "",
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val pages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Opciones de filtros por grado y sección
    val isMetaLoading: Boolean = false,
    val gradesOptions: List<String> = emptyList(),
    val sectionsOptions: List<String> = emptyList(),
    val sectionsByGrade: Map<String, List<String>> = emptyMap(),
    val selectedGrade: String? = null,
    val selectedSection: String? = null
)

class StudentsViewModel(
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState
    private var gradeNameToId: Map<String, Int> = emptyMap()
    private var sectionNameToId: Map<String, Int> = emptyMap()

    init {
        fetchStudents()
        loadMetaOptions()
        // Auto-actualización periódica cada 30 segundos
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                fetchStudents(page = 1)
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun fetchStudents(page: Int? = null) {
        val targetPage = page ?: _uiState.value.page
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val gradeId = _uiState.value.selectedGrade?.let { gradeNameToId[it] }
            val sectionId = _uiState.value.selectedSection?.let { sectionNameToId[it] }
            when (val result = getStudents(
                targetPage,
                _uiState.value.perPage,
                _uiState.value.query.ifBlank { null },
                gradeId,
                sectionId
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

    // Refresco manual desde la UI
    fun refresh() {
        fetchStudents(page = 1)
    }

    // Carga de opciones de grado y sección por sede
    private fun loadMetaOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMetaLoading = true) }
            val gradesResp = RetrofitClient.apiService.getGradesByBranch(page = 1, perPage = 200)
            val gradeItems = gradesResp.body()?.data?.items ?: emptyList()
            gradeNameToId = gradeItems.associate { it.nombre to it.id }
            val gradesBranch = gradeItems.map { it.nombre }.distinct().sorted()
            _uiState.update {
                it.copy(
                    isMetaLoading = false,
                    gradesOptions = gradesBranch,
                    sectionsOptions = emptyList(),
                    sectionsByGrade = emptyMap()
                )
            }
        }
    }

    fun loadSectionsForGrade(gradeName: String?) {
        if (gradeName.isNullOrBlank()) return
        val gradeId = gradeNameToId[gradeName] ?: return
        viewModelScope.launch {
            val resp = RetrofitClient.apiService.getSectionsByBranch(page = 1, perPage = 200, gradeId = gradeId)
            val items = resp.body()?.data?.items ?: emptyList()
            val sections = items.map { it.nombre }.distinct().sorted()
            sectionNameToId = items.associate { it.nombre to it.id }
            val currentMap = _uiState.value.sectionsByGrade.toMutableMap()
            currentMap[gradeName] = sections
            _uiState.update { it.copy(sectionsByGrade = currentMap, sectionsOptions = sections) }
        }
    }

    fun onGradeSelected(value: String?) {
        _uiState.update { it.copy(selectedGrade = value, selectedSection = null) }
        loadSectionsForGrade(value)
        fetchStudents(page = 1)
    }

    fun onSectionSelected(value: String?) {
        _uiState.update { it.copy(selectedSection = value) }
        fetchStudents(page = 1)
    }
}

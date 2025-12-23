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
    val isAppending: Boolean = false, // Carga de siguientes páginas
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
    
    // Control para paginación inversa (Backend DESC -> Frontend ASC)
    private var nextReversePage: Int = 0

    init {
        fetchStudents(page = 1)
        loadMetaOptions()
        // Auto-actualización periódica cada 30 segundos
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                // Solo refrescar si estamos en la vista inicial para evitar saltos raros
                if (!uiState.value.isAppending && nextReversePage < uiState.value.pages) {
                     // Opcional: Podríamos implementar un refresco inteligente, 
                     // pero con paginación inversa es complejo. Por ahora lo dejamos pausado o solo refresh total.
                     // fetchStudents(page = 1) 
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        fetchStudents(page = 1)
    }

    fun fetchStudents(page: Int? = null) {
        // Si page es 1 o null, iniciamos el proceso de "Reverse Pagination"
        val isReset = page == 1 || page == null
        
        viewModelScope.launch {
            if (isReset) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, students = emptyList(), page = 1) }
                
                // 1. Obtener metadatos (Total de páginas) usando la página 1
                val gradeId = _uiState.value.selectedGrade?.let { gradeNameToId[it] }
                val sectionId = _uiState.value.selectedSection?.let { sectionNameToId[it] }
                
                val metaResult = getStudents(
                    1,
                    _uiState.value.perPage,
                    _uiState.value.query.ifBlank { null },
                    gradeId,
                    sectionId
                )
                
                if (metaResult is Result.Success) {
                    val totalPages = metaResult.data.pages
                    if (totalPages == 0) {
                        // No hay datos
                        _uiState.update { 
                            it.copy(isLoading = false, students = emptyList(), total = 0, pages = 0) 
                        }
                        return@launch
                    }
                    
                    // 2. Calcular la última página (que tiene los registros más antiguos -> ID 1, 2...)
                    val lastPage = totalPages
                    
                    // 3. Cargar esa última página
                    loadReversePage(lastPage, isFirstLoad = true)
                    
                    // 4. Configurar siguiente página a cargar (hacia atrás)
                    nextReversePage = lastPage - 1
                } else if (metaResult is Result.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = metaResult.message) }
                }
                
            } else {
                // Carga de página específica (siguiente chunk en scroll infinito)
                page?.let { targetPage ->
                    loadReversePage(targetPage, isFirstLoad = false)
                    nextReversePage = targetPage - 1
                }
            }
        }
    }
    
    private suspend fun loadReversePage(page: Int, isFirstLoad: Boolean) {
        if (!isFirstLoad) {
            _uiState.update { it.copy(isAppending = true, errorMessage = null) }
        }

        val gradeId = _uiState.value.selectedGrade?.let { gradeNameToId[it] }
        val sectionId = _uiState.value.selectedSection?.let { sectionNameToId[it] }

        val result = getStudents(
            page,
            _uiState.value.perPage,
            _uiState.value.query.ifBlank { null },
            gradeId,
            sectionId
        )

        when (result) {
            is Result.Success -> {
                val pageData = result.data
                // REVERTIMOS la lista para que quede ASCENDENTE (1, 2, 3...)
                // Ya que el backend devuelve DESCENDENTE (20, 19, 18...)
                val sortedItems = pageData.items.reversed() // O sortedBy { it.id }
                
                _uiState.update {
                    it.copy(
                        students = it.students + sortedItems,
                        page = pageData.page, // Mantenemos ref de la página API, aunque no se use para next
                        perPage = pageData.perPage,
                        total = pageData.total,
                        pages = pageData.pages,
                        isLoading = false,
                        isAppending = false,
                        errorMessage = null
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isAppending = false, 
                        errorMessage = result.message
                    ) 
                }
            }
            is Result.Loading -> {}
        }
    }
    
    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.isAppending) return
        
        // En modo reverso, cargamos la página anterior (nextReversePage)
        if (nextReversePage >= 1) {
            fetchStudents(page = nextReversePage)
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

package com.jotadev.aiapaec.ui.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.api.GradeDto
import com.jotadev.aiapaec.data.api.SectionDto
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
    // Reemplazamos el mapa simple por un caché estructurado: Grado -> (Sección -> ID)
    private val sectionIdsCache = mutableMapOf<String, Map<String, Int>>()
    private var gradesList: List<GradeDto> = emptyList() // Caché de grados con sus secciones
    
    // Control para paginación estándar (Backend ordena por ID ASC)
    
    init {
        fetchStudents(page = 1)
        loadMetaOptions()
        // Auto-actualización periódica cada 30 segundos
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                // Refresco silencioso solo si estamos en la primera página y no hay carga activa
                if (!uiState.value.isLoading && !uiState.value.isAppending && uiState.value.page == 1) {
                     // fetchStudents(page = 1) // Comentado por seguridad
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        fetchStudents(page = 1)
    }

    fun fetchStudents(page: Int = 1, isPagination: Boolean = false) {
        val isReset = page == 1
        
        viewModelScope.launch {
            if (isReset) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, students = emptyList(), page = 1) }
            } else {
                    _uiState.update { it.copy(isAppending = true, errorMessage = null) }
                }
                val gradeId = _uiState.value.selectedGrade?.let { gradeNameToId[it] }
                
                // Obtener ID de sección desde el caché estructurado
                // Esto garantiza que el ID corresponda al grado seleccionado y no a uno anterior
                val sectionId = _uiState.value.selectedGrade?.let { grade ->
                    _uiState.value.selectedSection?.let { section ->
                        sectionIdsCache[grade]?.get(section)
                    }
                }

                // Detectar si la búsqueda es por ID (numérico) o por texto
                val rawQuery = _uiState.value.query.trim()
            val idFilter = rawQuery.toIntOrNull()
            val textQuery = if (idFilter == null && rawQuery.isNotBlank()) rawQuery else null

            val result = getStudents(
                page,
                _uiState.value.perPage,
                textQuery,
                idFilter,
                gradeId,
                sectionId,
                sortBy = "id",
                order = "asc"
            )
            
            when (result) {
                is Result.Success -> {
                    val pageData = result.data
                    _uiState.update {
                        it.copy(
                            students = if (isReset) pageData.items else it.students + pageData.items,
                            page = pageData.page,
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
    }
    
    // Método auxiliar eliminado (loadReversePage) ya que usamos paginación estándar
    
    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.isAppending) return
        
        // Paginación estándar: si no estamos en la última página, cargamos la siguiente
        if (_uiState.value.page < _uiState.value.pages) {
            fetchStudents(page = _uiState.value.page + 1, isPagination = true)
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
            try {
                val gradesResp = RetrofitClient.apiService.getGradesByBranch(page = 1, perPage = 200)
                val gradeItems = gradesResp.body()?.data?.items ?: emptyList()
                gradesList = gradeItems // Guardamos la lista completa para acceso local a secciones
                gradeNameToId = gradeItems.associate { it.nombre to it.id }
                val gradesBranch = gradeItems.map { it.nombre }.distinct().sorted()
                
                // Pre-cargar secciones si vienen en el objeto GradeDto
                val sectionsMap = gradeItems.associate { grade ->
                    val sMap = grade.sections.associate { it.nombre to it.id }
                    if (sMap.isNotEmpty()) {
                        sectionIdsCache[grade.nombre] = sMap
                    }
                    grade.nombre to grade.sections.map { it.nombre }.sorted()
                }
                
                _uiState.update {
                    it.copy(
                        isMetaLoading = false,
                        gradesOptions = gradesBranch,
                        sectionsOptions = emptyList(),
                        sectionsByGrade = sectionsMap
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isMetaLoading = false) }
            }
        }
    }

    fun loadSectionsForGrade(gradeName: String?) {
        if (gradeName.isNullOrBlank()) return
        val gradeId = gradeNameToId[gradeName] ?: return
        
        viewModelScope.launch {
            // ESTRATEGIA MIXTA:
            // 1. Intentar obtener secciones desde la caché local (gradesList) si existen
            val cachedGrade = gradesList.find { it.id == gradeId }
            val cachedSections = cachedGrade?.sections ?: emptyList()
            
            if (cachedSections.isNotEmpty()) {
                val sectionsNames = cachedSections.map { it.nombre }.distinct().sorted()
                // Actualizamos el caché de IDs para este grado específico
                sectionIdsCache[gradeName] = cachedSections.associate { it.nombre to it.id }
                
                val currentMap = _uiState.value.sectionsByGrade.toMutableMap()
                currentMap[gradeName] = sectionsNames
                _uiState.update { it.copy(sectionsByGrade = currentMap, sectionsOptions = sectionsNames) }
            } else {
                // 2. Fallback: Llamada a API si no hay secciones anidadas
                try {
                    val resp = RetrofitClient.apiService.getSectionsByBranch(page = 1, perPage = 200, gradeId = gradeId)
                    val allItems = resp.body()?.data?.items ?: emptyList()
                    
                    // Filtrar secciones que correspondan al grado solicitado
                    // Esto es crítico porque el endpoint podría devolver secciones de otros grados
                    val hasGradeInfo = allItems.any { it.gradeId != null }
                    val items = if (hasGradeInfo) {
                        allItems.filter { it.gradeId == gradeId }
                    } else {
                        allItems
                    }
                    
                    // SEGURIDAD: Validar integridad de los datos
                    // Si no tenemos gradeId explícito (backend desactualizado) y detectamos nombres duplicados,
                    // significa que recibimos TODAS las secciones (backend ignoró el filtro).
                    // En este caso, NO debemos actualizar la caché con datos corruptos.
                    if (!hasGradeInfo) {
                        val names = items.map { it.nombre }
                        val hasDuplicates = names.size != names.distinct().size
                        if (hasDuplicates) {
                            // Si detectamos ambigüedad, preferimos conservar la caché existente (si existe) o abortar
                            if (sectionIdsCache[gradeName]?.isNotEmpty() == true) {
                                return@launch
                            }
                            // Si no hay caché previa, es mejor no mostrar secciones que mostrar secciones erróneas
                            return@launch
                        }
                    }
                    
                    val sections = items.map { it.nombre }.distinct().sorted()
                    
                    // Actualizamos el caché de IDs para este grado específico
                    sectionIdsCache[gradeName] = items.associate { it.nombre to it.id }
                    
                    val currentMap = _uiState.value.sectionsByGrade.toMutableMap()
                    currentMap[gradeName] = sections
                    _uiState.update { it.copy(sectionsByGrade = currentMap, sectionsOptions = sections) }
                } catch (e: Exception) {
                    // Manejo silencioso de error en carga de secciones
                }
            }
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

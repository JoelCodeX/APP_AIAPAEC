package com.jotadev.aiapaec.ui.screens.format

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jotadev.aiapaec.data.api.RetrofitClient

data class FormatItem(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: String,
    val grade: String = "",
    val section: String = "",
    val numQuestions: Int = 0,
    val formatType: String = "",
    val scoreFormat: String = ""
)

data class FormatUiState(
    val isLoading: Boolean = false,
    val isMetaLoading: Boolean = false,
    val formats: List<FormatItem> = emptyList(),
    val allFormats: List<FormatItem> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val gradesOptions: List<String> = emptyList(),
    val sectionsOptions: List<String> = emptyList(),
    val sectionsByGrade: Map<String, List<String>> = emptyMap()
)

class FormatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FormatUiState())
    val uiState: StateFlow<FormatUiState> = _uiState.asStateFlow()

    // MAPA INTERNO PARA OBTENER ID DE GRADO DESDE NOMBRE
    private var gradeNameToId: Map<String, Int> = emptyMap()

    init {
        loadFormats()
        loadMetaOptions()
    }

    private fun loadFormats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resp = RetrofitClient.apiService.getWeeklyAssignments(page = 1, perPage = 50)
                val page = resp.body()?.data
                val items = page?.items?.map { dto ->
                    val name = "Formato ${dto.formato_nombre ?: ""} ${dto.numero_preguntas} preguntas"
                    FormatItem(
                        id = dto.id.toString(),
                        name = name,
                        description = "Formato ${dto.formato_nombre ?: ""} de ${dto.numero_preguntas} preguntas",
                        createdAt = "",
                        grade = dto.grado_nombre ?: "",
                        section = dto.seccion_nombre ?: "",
                        numQuestions = dto.numero_preguntas,
                        formatType = dto.formato_nombre ?: "",
                        scoreFormat = (dto.puntaje ?: 0.0).toString()
                    )
                } ?: emptyList()
                _uiState.value = _uiState.value.copy(isLoading = false, formats = items, allFormats = items)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadMetaOptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMetaLoading = true)
            try {
                // Cargar SIEMPRE los grados por sede; las secciones se cargan por grado
                val gradesResp = RetrofitClient.apiService.getGradesByBranch(page = 1, perPage = 200)
                val gradeItems = gradesResp.body()?.data?.items ?: emptyList()
                gradeNameToId = gradeItems.associate { it.nombre to it.id }
                val gradesBranch = gradeItems.map { it.nombre }.distinct().sorted()

                _uiState.value = _uiState.value.copy(
                    isMetaLoading = false,
                    gradesOptions = gradesBranch,
                    sectionsOptions = emptyList(),
                    sectionsByGrade = emptyMap()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isMetaLoading = false)
            }
        }
    }

    // RECARGAR OPCIONES DE GRADOS Y SECCIONES
    fun reloadMetaOptions() {
        loadMetaOptions()
    }

    fun refreshFormats() {
        loadFormats()
    }

    // CARGA SECCIONES AL SELECCIONAR UN GRADO (FALLBACK POR SEDE)
    fun loadSectionsForGrade(gradeName: String?) {
        if (gradeName.isNullOrBlank()) return
        val gradeId = gradeNameToId[gradeName] ?: return
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.apiService.getSectionsByBranch(page = 1, perPage = 200, gradeId = gradeId)
                val items = resp.body()?.data?.items ?: emptyList()
                val sections = items.map { it.nombre }.distinct().sorted()
                val currentMap = _uiState.value.sectionsByGrade.toMutableMap()
                currentMap[gradeName] = sections
                _uiState.value = _uiState.value.copy(sectionsByGrade = currentMap, sectionsOptions = sections)
            } catch (e: Exception) {
                // SIN MANEJO DE ERRORES SEGÚN REGLAS
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val base = _uiState.value.allFormats
        _uiState.value = _uiState.value.copy(
            formats = if (query.isBlank()) base else base.filter { it.name.contains(query, true) || it.description.contains(query, true) }
        )
    }

    fun loadFormats(
        query: String? = null,
        grade: String? = null,
        section: String? = null,
        numQuestions: Int? = null,
        formatType: String? = null,
        scoreFormat: String? = null
    ) {
        val source = _uiState.value.allFormats
        var result = source
        query?.let { q ->
            result = result.filter { it.name.contains(q, true) || it.description.contains(q, true) }
        }
        grade?.let { g -> if (g.isNotBlank()) result = result.filter { it.grade == g } }
        section?.let { s -> if (s.isNotBlank()) result = result.filter { it.section == s } }
        numQuestions?.let { n -> if (n > 0) result = result.filter { it.numQuestions == n } }
        formatType?.let { f -> if (f.isNotBlank()) result = result.filter { it.formatType == f } }
        scoreFormat?.let { sf -> if (sf.isNotBlank()) result = result.filter { it.scoreFormat == sf } }
        _uiState.value = _uiState.value.copy(formats = result)
    }

    fun createFormat(
        name: String,
        grade: String,
        section: String,
        numQuestions: Int,
        formatType: String,
        scoreFormat: String
    ) {
        viewModelScope.launch {
            try {
                val req = com.jotadev.aiapaec.data.api.CreateWeeklyAssignmentRequest(
                    grade = grade,
                    section = section,
                    num_questions = numQuestions,
                    format_type = formatType,
                    score_format = scoreFormat
                )
                val resp = RetrofitClient.apiService.createWeeklyAssignment(req)
                val dto = resp.body()?.data
                if (dto != null) {
                    val generatedName = "Formato ${dto.formato_nombre ?: ""} ${dto.numero_preguntas} preguntas"
                    val newItem = FormatItem(
                        id = dto.id.toString(),
                        name = generatedName,
                        description = "Formato ${dto.formato_nombre ?: ""} de ${dto.numero_preguntas} preguntas",
                        createdAt = java.time.LocalDate.now().toString(),
                        grade = dto.grado_nombre ?: grade,
                        section = dto.seccion_nombre ?: section,
                        numQuestions = dto.numero_preguntas,
                        formatType = dto.formato_nombre ?: formatType,
                        scoreFormat = (dto.puntaje ?: 0.0).toString()
                    )
                    val updatedAll = _uiState.value.allFormats.toMutableList().apply { add(0, newItem) }
                    _uiState.value = _uiState.value.copy(allFormats = updatedAll, formats = updatedAll)
                }
            } catch (e: Exception) {
                // SIN MANEJO DE ERRORES SEGÚN REGLAS
            }
        }
    }

    fun deleteFormat(id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteWeeklyAssignment(id.toInt())
                val updatedAll = _uiState.value.allFormats.filterNot { it.id == id }
                _uiState.value = _uiState.value.copy(allFormats = updatedAll, formats = updatedAll)
            } catch (e: Exception) {
                // SIN MANEJO DE ERRORES SEGÚN REGLAS
            }
        }
    }

    fun updateFormat(
        id: String,
        grade: String,
        section: String,
        numQuestions: Int,
        formatType: String,
        scoreFormat: String
    ) {
        viewModelScope.launch {
            try {
                val req = com.jotadev.aiapaec.data.api.UpdateWeeklyAssignmentRequest(
                    grade = grade,
                    section = section,
                    num_questions = numQuestions,
                    format_type = formatType,
                    score_format = scoreFormat
                )
                val resp = RetrofitClient.apiService.updateWeeklyAssignment(id.toInt(), req)
                val dto = resp.body()?.data
                if (dto != null) {
                    val newItem = FormatItem(
                        id = dto.id.toString(),
                        name = "Formato ${dto.formato_nombre ?: ""} ${dto.numero_preguntas} preguntas",
                        description = "Formato ${dto.formato_nombre ?: ""} de ${dto.numero_preguntas} preguntas",
                        createdAt = "",
                        grade = dto.grado_nombre ?: grade,
                        section = dto.seccion_nombre ?: section,
                        numQuestions = dto.numero_preguntas,
                        formatType = dto.formato_nombre ?: formatType,
                        scoreFormat = (dto.puntaje ?: 0.0).toString()
                    )
                    val list = _uiState.value.allFormats.toMutableList()
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx >= 0) {
                        list[idx] = newItem
                        _uiState.value = _uiState.value.copy(allFormats = list, formats = list)
                    }
                }
            } catch (e: Exception) {
                // SIN MANEJO DE ERRORES SEGÚN REGLAS
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

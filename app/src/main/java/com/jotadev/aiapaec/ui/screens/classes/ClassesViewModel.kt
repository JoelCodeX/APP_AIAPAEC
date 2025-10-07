package com.jotadev.aiapaec.ui.screens.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.SchoolClass
import com.jotadev.aiapaec.domain.usecases.GetClassesUseCase
import com.jotadev.aiapaec.data.repository.ClassesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

data class ClassesUiState(
    val classes: List<SchoolClass> = emptyList(),
    val query: String = "",
    val selectedLevel: String = "Todos los niveles",
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val pages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ClassesViewModel(
    private val getClasses: GetClassesUseCase = GetClassesUseCase(ClassesRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState

    init {
        fetchClasses()
        // Auto-actualización periódica cada 30 segundos
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                // Mantiene filtros actuales y trae la primera página
                fetchClasses(page = 1)
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun onLevelChange(value: String) {
        _uiState.update { it.copy(selectedLevel = value) }
    }

    private fun mapLevelForApi(level: String): String? {
        return when (level) {
            "Primaria" -> "Primary"
            "Secundaria" -> "Secondary"
            else -> null
        }
    }

    fun fetchClasses(page: Int? = null) {
        val targetPage = page ?: _uiState.value.page
        val apiLevel = mapLevelForApi(_uiState.value.selectedLevel)
        val queryOrNull = _uiState.value.query.ifBlank { null }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getClasses(targetPage, _uiState.value.perPage, queryOrNull, apiLevel)) {
                is Result.Success -> {
                    val pageData = result.data
                    _uiState.update {
                        it.copy(
                            classes = pageData.items,
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

    // Refresco manual desde la UI (gesto pull-to-refresh)
    fun refresh() {
        fetchClasses(page = 1)
    }
}
package com.jotadev.aiapaec.ui.screens.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Grade
import com.jotadev.aiapaec.domain.usecases.GetGradesByBranchUseCase
import com.jotadev.aiapaec.data.repository.GradesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

data class ClassesUiState(
    val grades: List<Grade> = emptyList(),
    val gradesOptions: List<String> = emptyList(),
    val selectedGrade: String = "Todos los grados",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GradesViewModel(
    private val getGrades: GetGradesByBranchUseCase = GetGradesByBranchUseCase(GradesRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState

    init {
        loadGrades()
    }

    fun onGradeChange(value: String) {
        _uiState.update { it.copy(selectedGrade = value) }
    }

    private fun loadGrades() {
        viewModelScope.launch {
            when (val result = getGrades(page = 1, perPage = 200)) {
                is Result.Success -> {
                    val items = result.data.items
                    val names = items.map { it.nombre }.distinct().sorted()
                    val options = listOf("Todos los grados") + names
                    _uiState.update { it.copy(grades = items, gradesOptions = options) }
                }
                is Result.Error -> {
                    // Sin manejo de errores según reglas, dejar opciones vacías
                }
                is Result.Loading -> { }
            }
        }
    }
}

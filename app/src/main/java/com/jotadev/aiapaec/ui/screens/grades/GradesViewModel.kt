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
    val selectedLevel: String = "Todos los niveles",
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

    fun onLevelChange(value: String) {
        _uiState.update { currentState ->
            val filteredGrades = if (value == "Todos los niveles") {
                currentState.grades
            } else {
                currentState.grades.filter { it.nivel == value }
            }
            val newOptions = listOf("Todos los grados") + filteredGrades.map { it.nombre }.distinct().sorted()
            
            currentState.copy(
                selectedLevel = value,
                gradesOptions = newOptions,
                selectedGrade = "Todos los grados"
            )
        }
    }

    private fun loadGrades() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getGrades(page = 1, perPage = 200)) {
                is Result.Success -> {
                    val items = result.data.items
                    // Initial options based on default "Todos los niveles"
                    val names = items.map { it.nombre }.distinct().sorted()
                    val options = listOf("Todos los grados") + names
                    _uiState.update { 
                        it.copy(
                            grades = items, 
                            gradesOptions = options,
                            isLoading = false
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception?.message
                        ) 
                    }
                }
                is Result.Loading -> { 
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}

package com.jotadev.aiapaec.ui.screens.format.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.domain.models.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeeklyUiState(
    val isLoading: Boolean = false,
    val createdQuiz: Quiz? = null,
    val message: String? = null
)

class WeeklyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeeklyUiState())
    val uiState: StateFlow<WeeklyUiState> = _uiState.asStateFlow()

    private val repo = QuizzesRepositoryImpl()

    fun createQuiz(
        title: String,
        bimesterId: Int?,
        unidadId: Int?,
        fecha: String,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val res = repo.createQuiz(
                    title = title,
                    bimesterId = bimesterId,
                    unidadId = unidadId,
                    gradoId = null,
                    seccionId = null,
                    fecha = fecha,
                    numQuestions = numQuestions,
                    detalle = detalle,
                    asignacionId = asignacionId
                )
                when (res) {
                    is com.jotadev.aiapaec.domain.models.Result.Success -> _uiState.value = WeeklyUiState(isLoading = false, createdQuiz = res.data)
                    is com.jotadev.aiapaec.domain.models.Result.Error -> _uiState.value = WeeklyUiState(isLoading = false, message = res.message)
                }
            } catch (_: Exception) {
                _uiState.value = WeeklyUiState(isLoading = false)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}


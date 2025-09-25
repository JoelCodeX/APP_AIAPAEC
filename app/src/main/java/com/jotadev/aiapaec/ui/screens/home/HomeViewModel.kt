package com.jotadev.aiapaec.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Usuario",
    val recentActivities: List<String> = emptyList(),
    val quickActions: List<QuickAction> = emptyList()
)

data class QuickAction(
    val title: String,
    val description: String,
    val action: () -> Unit
)

class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simular carga de datos
            val quickActions = listOf(
                QuickAction(
                    title = "Nuevo Examen",
                    description = "Crear un nuevo examen",
                    action = { /* Navegar a crear examen */ }
                ),
                QuickAction(
                    title = "Ver Resultados",
                    description = "Revisar resultados recientes",
                    action = { /* Navegar a resultados */ }
                ),
                QuickAction(
                    title = "Escanear Tarjeta",
                    description = "Escanear tarjeta de respuestas",
                    action = { /* Navegar a escáner */ }
                )
            )
            
            val recentActivities = listOf(
                "Examen de Matemáticas creado",
                "Resultados de Historia procesados",
                "Tarjeta escaneada exitosamente"
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                quickActions = quickActions,
                recentActivities = recentActivities
            )
        }
    }
    
    fun refreshData() {
        loadHomeData()
    }
}
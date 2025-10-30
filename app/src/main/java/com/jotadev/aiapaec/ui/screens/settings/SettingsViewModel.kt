package com.jotadev.aiapaec.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jotadev.aiapaec.data.storage.UserStorage

data class SettingsItem(
    val id: String,
    val title: String,
    val description: String,
    val type: SettingsType,
    val value: Any? = null,
    val isEnabled: Boolean = true
)

enum class SettingsType {
    TOGGLE, SELECTION, ACTION, INFO
}

data class UserProfile(
    val name: String,
    val email: String,
    val institution: String,
    val role: String
)

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val generalSettings: List<SettingsItem> = emptyList(),
    val examSettings: List<SettingsItem> = emptyList(),
    val systemSettings: List<SettingsItem> = emptyList(),
    val error: String? = null
)

class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val storedName = UserStorage.getName() ?: "Usuario AIAPAEC"
                val storedEmail = UserStorage.getEmail() ?: "usuario@aiapaec.edu.pe"
                val storedInstitution = UserStorage.getInstitution() ?: "AIAPAEC"
                val storedRole = UserStorage.getRole() ?: "Docente"
                val userProfile = UserProfile(
                    name = storedName,
                    email = storedEmail,
                    institution = storedInstitution,
                    role = storedRole
                )
                
                val generalSettings = listOf(
                    SettingsItem("notifications", "Notificaciones", "Recibir notificaciones push", SettingsType.TOGGLE, true),
                    SettingsItem("language", "Idioma", "Español", SettingsType.SELECTION, "es"),
                    SettingsItem("theme", "Tema", "Claro", SettingsType.SELECTION, "light")
                )
                
                val examSettings = listOf(
                    SettingsItem("auto_save", "Guardado automático", "Guardar automáticamente cada 5 minutos", SettingsType.TOGGLE, true),
                    SettingsItem("default_time", "Tiempo por defecto", "60 minutos", SettingsType.SELECTION, 60),
                    SettingsItem("show_results", "Mostrar resultados", "Mostrar resultados inmediatamente", SettingsType.TOGGLE, false)
                )
                
                val systemSettings = listOf(
                    SettingsItem("backup", "Respaldo de datos", "Crear respaldo", SettingsType.ACTION),
                    SettingsItem("sync", "Sincronización", "Sincronizar con la nube", SettingsType.ACTION),
                    SettingsItem("version", "Versión", "1.0.0", SettingsType.INFO),
                    SettingsItem("logout", "Cerrar sesión", "Salir de la aplicación", SettingsType.ACTION)
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = userProfile,
                    generalSettings = generalSettings,
                    examSettings = examSettings,
                    systemSettings = systemSettings
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar configuraciones: ${e.message}"
                )
            }
        }
    }
    
    fun updateSetting(settingId: String, newValue: Any) {
        viewModelScope.launch {
            // Actualizar configuración específica
            val currentState = _uiState.value
            
            val updatedGeneralSettings = currentState.generalSettings.map { setting ->
                if (setting.id == settingId) setting.copy(value = newValue) else setting
            }
            
            val updatedExamSettings = currentState.examSettings.map { setting ->
                if (setting.id == settingId) setting.copy(value = newValue) else setting
            }
            
            _uiState.value = currentState.copy(
                generalSettings = updatedGeneralSettings,
                examSettings = updatedExamSettings
            )
        }
    }
    
    fun performAction(actionId: String) {
        viewModelScope.launch {
            when (actionId) {
                "backup" -> createBackup()
                "sync" -> syncData()
                "logout" -> logout()
            }
        }
    }
    
    private suspend fun createBackup() {
        // Lógica para crear respaldo
    }
    
    private suspend fun syncData() {
        // Lógica para sincronizar datos
    }
    
    private suspend fun logout() {
        // Lógica para cerrar sesión
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
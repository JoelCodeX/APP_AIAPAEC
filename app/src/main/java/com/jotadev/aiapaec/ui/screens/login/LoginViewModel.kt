package com.jotadev.aiapaec.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val mostrarContrasena: Boolean = false,
    val recordarUsuario: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val userToken: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val authRepository = AuthRepository()

    fun updateUsuario(usuario: String) {
        _uiState.value = _uiState.value.copy(usuario = usuario)
    }

    fun updateContrasena(contrasena: String) {
        _uiState.value = _uiState.value.copy(contrasena = contrasena)
    }

    fun toggleMostrarContrasena() {
        _uiState.value = _uiState.value.copy(
            mostrarContrasena = !_uiState.value.mostrarContrasena
        )
    }
    
    fun toggleRecordarUsuario() {
        _uiState.value = _uiState.value.copy(
            recordarUsuario = !_uiState.value.recordarUsuario
        )
    }

    fun login() {
        val currentState = _uiState.value
        
        // VALIDACIONES BÁSICAS
        if (currentState.usuario.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "EL EMAIL NO PUEDE ESTAR VACÍO")
            return
        }
        
        if (!isValidEmail(currentState.usuario)) {
            _uiState.value = currentState.copy(errorMessage = "FORMATO DE EMAIL INVÁLIDO")
            return
        }
        
        if (currentState.contrasena.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "LA CONTRASEÑA NO PUEDE ESTAR VACÍA")
            return
        }
        
        if (currentState.contrasena.length < 6) {
            _uiState.value = currentState.copy(errorMessage = "LA CONTRASEÑA DEBE TENER AL MENOS 6 CARACTERES")
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            authRepository.login(currentState.usuario, currentState.contrasena)
                .onSuccess { loginResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        userToken = loginResponse.token,
                        errorMessage = null,
                        usuario = "",
                        contrasena = "",
                        mostrarContrasena = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = false,
                        errorMessage = exception.message ?: "ERROR DESCONOCIDO"
                    )
                }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearFields() {
        _uiState.value = _uiState.value.copy(
            usuario = "",
            contrasena = "",
            mostrarContrasena = false,
            errorMessage = null
        )
    }
}
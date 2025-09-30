package com.jotadev.aiapaec.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.usecases.LoginUseCase
import com.jotadev.aiapaec.data.repository.AuthRepositoryImpl
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import javax.inject.Inject

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

// @HiltViewModel
class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(AuthRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            when (val result = loginUseCase(currentState.usuario, currentState.contrasena)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        userToken = result.data.token,
                        errorMessage = null,
                        usuario = "",
                        contrasena = "",
                        mostrarContrasena = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {
                    // YA ESTÁ EN LOADING
                }
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
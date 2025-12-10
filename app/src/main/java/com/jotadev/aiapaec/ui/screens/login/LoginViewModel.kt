package com.jotadev.aiapaec.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.usecases.LoginUseCase
import com.jotadev.aiapaec.data.repository.AuthRepositoryImpl
import com.jotadev.aiapaec.data.storage.UserStorage
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

    fun setRecordarUsuario(remember: Boolean) {
        _uiState.value = _uiState.value.copy(recordarUsuario = remember)
        if (!remember) {
            // Limpiar email recordado si el usuario desactiva "Recordar"
            UserStorage.clearRememberedEmail()
            UserStorage.saveRememberFlag(false)
        } else {
            // Persistir bandera cuando el usuario activa "Recordar"
            UserStorage.saveRememberFlag(true)
        }
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
                    // GUARDAR PERFIL Y SEDE DEL USUARIO
                    val u = result.data.user
                    UserStorage.save(
                        name = u?.fullName,
                        email = u?.email,
                        institution = "AIAPAEC",
                        role = u?.role,
                        branchId = u?.branchId,
                        branchName = u?.branchName
                    )
                    // Guardar email ingresado SOLO para login si el usuario eligió recordar
                    if (currentState.recordarUsuario) {
                        UserStorage.saveRememberedEmail(currentState.usuario)
                        UserStorage.saveRememberFlag(true)
                    }
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
    
    fun isValidEmail(email: String): Boolean {
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

    // Precargar email recordado si existe
    fun prefillRememberedEmail() {
        val savedEmail = UserStorage.getRememberedEmail()
        val rememberFlag = UserStorage.getRememberFlag()
        if (rememberFlag && !savedEmail.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                usuario = savedEmail,
                recordarUsuario = true
            )
        }
    }
}

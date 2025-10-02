package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.mappers.toLoginRequest
import com.jotadev.aiapaec.data.mappers.toLoginResult
import com.jotadev.aiapaec.domain.models.LoginData
import com.jotadev.aiapaec.domain.models.LoginResult
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.AuthRepository
import com.jotadev.aiapaec.data.storage.TokenStorage

class AuthRepositoryImpl : AuthRepository {
    private val apiService = RetrofitClient.apiService
    
    override suspend fun login(loginData: LoginData): Result<LoginResult> {
        return try {
            val response = apiService.login(loginData.toLoginRequest())
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // EL BACKEND DEVUELVE "Login successful" EN EL CAMPO MESSAGE CUANDO ES EXITOSO
                    if (loginResponse.token != null && loginResponse.message.contains("successful", ignoreCase = true)) {
                        // Guardar token para futuras llamadas autenticadas
                        TokenStorage.saveToken(loginResponse.token)
                        Result.Success(loginResponse.toLoginResult())
                    } else {
                        Result.Error(loginResponse.message)
                    }
                } ?: Result.Error("RESPUESTA VACÍA DEL SERVIDOR")
            } else {
                // INTERPRETAR CÓDIGOS DE ERROR ESPECÍFICOS
                val errorMessage = when (response.code()) {
                    404 -> "USUARIO NO EXISTE"
                    401 -> "CONTRASEÑA INCORRECTA"
                    400 -> "DATOS INVÁLIDOS"
                    500 -> "ERROR INTERNO DEL SERVIDOR"
                    else -> "ERROR DEL SERVIDOR: ${response.code()}"
                }
                
                // INTENTAR EXTRAER MENSAJE DEL BACKEND
                try {
                    val errorBody = response.errorBody()?.string()
                    when {
                        errorBody?.contains("Usuario no existe") == true -> Result.Error("USUARIO NO EXISTE")
                        errorBody?.contains("Contraseña incorrecta") == true -> Result.Error("CONTRASEÑA INCORRECTA")
                        else -> Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Result.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // AQUÍ SE PODRÍA LLAMAR A UN ENDPOINT DE LOGOUT SI EXISTE
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("ERROR AL CERRAR SESIÓN: ${e.message}")
        }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        // IMPLEMENTAR LÓGICA PARA VERIFICAR SI EL USUARIO ESTÁ LOGUEADO
        // POR EJEMPLO, VERIFICAR SI HAY UN TOKEN GUARDADO
        return getStoredToken() != null
    }
    
    override suspend fun getStoredToken(): String? {
        // Obtener token desde almacenamiento local
        return TokenStorage.getToken()
    }
    
    override suspend fun clearUserSession(): Result<Unit> {
        return try {
            // Limpiar token almacenado
            TokenStorage.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("ERROR AL LIMPIAR SESIÓN: ${e.message}")
        }
    }
}
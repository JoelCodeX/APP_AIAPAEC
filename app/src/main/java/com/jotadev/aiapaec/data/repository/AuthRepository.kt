package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.LoginRequest
import com.jotadev.aiapaec.data.api.LoginResponse
import com.jotadev.aiapaec.data.api.RetrofitClient

class AuthRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // EL BACKEND DEVUELVE "Login successful" EN EL CAMPO MESSAGE CUANDO ES EXITOSO
                    // Y SIEMPRE INCLUYE UN TOKEN CUANDO ES EXITOSO
                    if (loginResponse.token != null && loginResponse.message.contains("successful", ignoreCase = true)) {
                        Result.success(loginResponse)
                    } else {
                        Result.failure(Exception(loginResponse.message))
                    }
                } ?: Result.failure(Exception("RESPUESTA VACÍA DEL SERVIDOR"))
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
                    if (errorBody?.contains("Usuario no existe") == true) {
                        Result.failure(Exception("USUARIO NO EXISTE"))
                    } else if (errorBody?.contains("Contraseña incorrecta") == true) {
                        Result.failure(Exception("CONTRASEÑA INCORRECTA"))
                    } else {
                        Result.failure(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("ERROR DE CONEXIÓN: ${e.message}"))
        }
    }
}
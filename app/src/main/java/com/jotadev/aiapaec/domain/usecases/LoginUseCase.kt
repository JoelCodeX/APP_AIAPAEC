package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.LoginData
import com.jotadev.aiapaec.domain.models.LoginResult
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResult> {
        // VALIDACIONES DE NEGOCIO
        if (email.isBlank()) {
            return Result.Error("EL EMAIL ES REQUERIDO")
        }
        
        if (password.isBlank()) {
            return Result.Error("LA CONTRASEÑA ES REQUERIDA")
        }
        
        if (!isValidEmail(email)) {
            return Result.Error("FORMATO DE EMAIL INVÁLIDO")
        }
        
        if (password.length < 6) {
            return Result.Error("LA CONTRASEÑA DEBE TENER AL MENOS 6 CARACTERES")
        }
        
        // EJECUTAR LOGIN
        val loginData = LoginData(email = email, password = password)
        return authRepository.login(loginData)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
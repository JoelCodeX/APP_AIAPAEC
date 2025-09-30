package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // LIMPIAR SESIÓN LOCAL
            authRepository.clearUserSession()
            // EJECUTAR LOGOUT EN EL SERVIDOR
            authRepository.logout()
        } catch (e: Exception) {
            Result.Error("ERROR AL CERRAR SESIÓN: ${e.message}")
        }
    }
}
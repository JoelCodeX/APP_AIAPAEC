package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.User
import com.jotadev.aiapaec.domain.repository.AuthRepository
import com.jotadev.aiapaec.domain.repository.UserRepository

class GetCurrentUserUseCase(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        // VERIFICAR SI EL USUARIO ESTÁ LOGUEADO
        if (!authRepository.isUserLoggedIn()) {
            return Result.Error("USUARIO NO AUTENTICADO")
        }
        
        // OBTENER INFORMACIÓN DEL USUARIO ACTUAL
        return userRepository.getCurrentUser()
    }
}
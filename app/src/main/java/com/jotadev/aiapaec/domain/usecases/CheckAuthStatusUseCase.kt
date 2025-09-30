package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.repository.AuthRepository

class CheckAuthStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn() && authRepository.getStoredToken() != null
    }
}
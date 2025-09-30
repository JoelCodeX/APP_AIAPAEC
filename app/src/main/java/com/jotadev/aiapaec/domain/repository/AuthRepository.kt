package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.LoginData
import com.jotadev.aiapaec.domain.models.LoginResult
import com.jotadev.aiapaec.domain.models.Result

interface AuthRepository {
    suspend fun login(loginData: LoginData): Result<LoginResult>
    suspend fun logout(): Result<Unit>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun getStoredToken(): String?
    suspend fun clearUserSession(): Result<Unit>
}
package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.User

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun getUserById(userId: Int): Result<User>
}
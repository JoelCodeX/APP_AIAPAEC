package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.mappers.toDomainUser
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.User
import com.jotadev.aiapaec.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    private val apiService = RetrofitClient.apiService
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            // TODO: IMPLEMENTAR ENDPOINT PARA OBTENER USUARIO ACTUAL
            // val response = apiService.getCurrentUser()
            // if (response.isSuccessful) {
            //     response.body()?.let { user ->
            //         Result.Success(user.toDomainUser())
            //     } ?: Result.Error("USUARIO NO ENCONTRADO")
            // } else {
            //     Result.Error("ERROR AL OBTENER USUARIO: ${response.code()}")
            // }
            
            // IMPLEMENTACIÓN TEMPORAL
            Result.Error("ENDPOINT NO IMPLEMENTADO")
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
    
    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            // TODO: IMPLEMENTAR ENDPOINT PARA ACTUALIZAR USUARIO
            Result.Error("ENDPOINT NO IMPLEMENTADO")
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
    
    override suspend fun getUserById(userId: Int): Result<User> {
        return try {
            // TODO: IMPLEMENTAR ENDPOINT PARA OBTENER USUARIO POR ID
            Result.Error("ENDPOINT NO IMPLEMENTADO")
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
}
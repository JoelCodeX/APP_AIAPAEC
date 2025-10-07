package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.ClassesPage
import com.jotadev.aiapaec.domain.repository.ClassesRepository

class ClassesRepositoryImpl : ClassesRepository {
    private val api = RetrofitClient.apiService

    override suspend fun getClasses(
        page: Int,
        perPage: Int,
        query: String?,
        level: String?
    ): Result<ClassesPage> {
        return try {
            val response = api.getClasses(page = page, perPage = perPage, query = query, level = level)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(body?.message ?: "RESPUESTA VACÍA DEL SERVIDOR")
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "TOKEN INVÁLIDO O EXPIRADO"
                    403 -> "ACCESO DENEGADO"
                    404 -> "RECURSO NO ENCONTRADO"
                    400 -> "SOLICITUD INVÁLIDA"
                    500 -> "ERROR DEL SERVIDOR"
                    else -> "ERROR DEL SERVIDOR: ${response.code()}"
                }
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
}
package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.ApiService
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.WeeksPage
import com.jotadev.aiapaec.domain.repository.WeeksRepository

class WeeksRepositoryImpl(private val apiService: ApiService) : WeeksRepository {
    override suspend fun getWeeks(page: Int, perPage: Int, unitId: Int?): Result<WeeksPage> {
        return try {
            val response = apiService.getWeeks(page, perPage, unitId)
            if (response.isSuccessful) {
                val body = response.body()
                val pageDto = body?.data
                if (body?.success == true && pageDto != null) {
                    Result.Success(pageDto.toDomain())
                } else {
                    Result.Error(body?.message ?: "Error al obtener semanas")
                }
            } else {
                Result.Error("Error de red: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}


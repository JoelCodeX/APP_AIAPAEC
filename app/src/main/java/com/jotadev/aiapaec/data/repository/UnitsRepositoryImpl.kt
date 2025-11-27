package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.ApiService
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.UnitsPage
import com.jotadev.aiapaec.domain.repository.UnitsRepository

class UnitsRepositoryImpl(private val apiService: ApiService) : UnitsRepository {
    override suspend fun getUnits(page: Int, perPage: Int, bimesterId: Int?): Result<UnitsPage> {
        return try {
            val response = apiService.getUnits(page, perPage, bimesterId)
            if (response.isSuccessful) {
                val body = response.body()
                val pageDto = body?.data
                if (body?.success == true && pageDto != null) {
                    Result.Success(pageDto.toDomain())
                } else {
                    Result.Error(body?.message ?: "Error al obtener unidades")
                }
            } else {
                Result.Error("Error de red: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}


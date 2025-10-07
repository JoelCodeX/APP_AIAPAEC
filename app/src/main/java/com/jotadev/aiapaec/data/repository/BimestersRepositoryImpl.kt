package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.ApiService
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.BimestersPage
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.BimestersRepository

class BimestersRepositoryImpl(private val apiService: ApiService) : BimestersRepository {
    override suspend fun getBimesters(
        page: Int,
        perPage: Int,
        query: String?,
        year: Int?
    ): Result<BimestersPage> {
        return try {
            val response = apiService.getBimesters(page, perPage, query, year)
            if (response.isSuccessful) {
                val body = response.body()
                val pageDto = body?.data
                if (body?.success == true && pageDto != null) {
                    Result.Success(pageDto.toDomain())
                } else {
                    Result.Error(body?.message ?: "Error al obtener bimestres")
                }
            } else {
                Result.Error("Error de red: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}
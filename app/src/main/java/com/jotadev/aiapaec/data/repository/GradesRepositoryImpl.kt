package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.GradesPage
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.GradesRepository

class GradesRepositoryImpl : GradesRepository {
    override suspend fun getGradesByBranch(page: Int, perPage: Int): Result<GradesPage> {
        return try {
            val response = RetrofitClient.apiService.getGradesByBranch(page = page, perPage = perPage)
            if (response.isSuccessful) {
                val body = response.body()
                val pageDto = body?.data
                if (body?.success == true && pageDto != null) {
                    Result.Success(pageDto.toDomain())
                } else {
                    Result.Error(body?.message ?: "Error al obtener grados")
                }
            } else {
                Result.Error("Error de red: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}

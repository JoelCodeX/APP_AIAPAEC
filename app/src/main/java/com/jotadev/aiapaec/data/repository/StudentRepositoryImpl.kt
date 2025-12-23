package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.StudentsPage
import com.jotadev.aiapaec.domain.repository.StudentRepository

class StudentRepositoryImpl : StudentRepository {
    private val api = RetrofitClient.apiService

    override suspend fun getStudents(
        page: Int,
        perPage: Int,
        query: String?,
        gradeId: Int?,
        sectionId: Int?,
        sortBy: String?,
        order: String?
    ): Result<StudentsPage> {
        return try {
            val response = api.getStudents(
                page = page,
                perPage = perPage,
                query = query,
                gradeId = gradeId,
                sectionId = sectionId,
                sortBy = sortBy,
                order = order
            )
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
                val backendMessage = try { response.errorBody()?.string() } catch (e: Exception) { null }
                Result.Error(backendMessage?.ifBlank { null } ?: errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }

    override suspend fun getStudent(id: Int): Result<Student> {
        return try {
            val response = api.getStudent(id)
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
                    404 -> "ESTUDIANTE NO ENCONTRADO"
                    400 -> "SOLICITUD INVÁLIDA"
                    500 -> "ERROR DEL SERVIDOR"
                    else -> "ERROR DEL SERVIDOR: ${response.code()}"
                }
                val backendMessage = try { response.errorBody()?.string() } catch (e: Exception) { null }
                Result.Error(backendMessage?.ifBlank { null } ?: errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("ERROR DE CONEXIÓN: ${e.message}")
        }
    }
}

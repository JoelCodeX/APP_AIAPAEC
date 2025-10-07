package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.api.CreateQuizRequest
import com.jotadev.aiapaec.data.api.UpdateQuizRequest
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class QuizzesRepositoryImpl : QuizzesRepository {
    private val api = RetrofitClient.apiService

    override suspend fun getQuizzes(page: Int, perPage: Int, query: String?, classId: Int?, bimesterId: Int?): Result<QuizzesPage> {
        return try {
            val response = api.getQuizzes(page = page, perPage = perPage, query = query, classId = classId, bimesterId = bimesterId)
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

    override suspend fun getQuiz(id: Int): Result<Quiz> {
        return try {
            val response = api.getQuiz(id)
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

    override suspend fun createQuiz(
        title: String,
        description: String?,
        classId: Int,
        bimesterId: Int,
        totalPoints: Double?,
        numQuestions: Int?,
        pointsPerQuestion: Double?,
        answerKeyFile: String?,
        keyVersion: String?
    ): Result<Quiz> {
        return try {
            val request = CreateQuizRequest(
                title = title,
                description = description,
                class_id = classId,
                bimester_id = bimesterId,
                total_points = totalPoints,
                num_questions = numQuestions,
                points_per_question = pointsPerQuestion,
                answer_key_file = answerKeyFile,
                key_version = keyVersion
            )
            val response = api.createQuiz(request)
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

    override suspend fun updateQuiz(
        id: Int,
        title: String?,
        description: String?,
        classId: Int?,
        bimesterId: Int?,
        totalPoints: Double?,
        numQuestions: Int?,
        pointsPerQuestion: Double?,
        answerKeyFile: String?,
        keyVersion: String?
    ): Result<Quiz> {
        return try {
            val request = UpdateQuizRequest(
                title = title,
                description = description,
                class_id = classId,
                bimester_id = bimesterId,
                total_points = totalPoints,
                num_questions = numQuestions,
                points_per_question = pointsPerQuestion,
                answer_key_file = answerKeyFile,
                key_version = keyVersion
            )
            val response = api.updateQuiz(id, request)
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

    override suspend fun deleteQuiz(id: Int): Result<Unit> {
        return try {
            val response = api.deleteQuiz(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.Success(Unit)
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
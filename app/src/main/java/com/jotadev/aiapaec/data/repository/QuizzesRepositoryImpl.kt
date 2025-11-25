package com.jotadev.aiapaec.data.repository

import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.api.CreateQuizRequest
import com.jotadev.aiapaec.data.api.UpdateQuizRequest
import com.jotadev.aiapaec.data.mappers.toDomain
import com.jotadev.aiapaec.data.api.UpdateAnswersRequest
import com.jotadev.aiapaec.data.api.UpdateAnswerItem
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.models.AnswerKey
import com.jotadev.aiapaec.domain.models.AnswerKeysPage
import com.jotadev.aiapaec.domain.models.QuizAnswersPage
import com.jotadev.aiapaec.domain.repository.QuizzesRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class QuizzesRepositoryImpl : QuizzesRepository {
    private val api = RetrofitClient.apiService

    override suspend fun getQuizzes(page: Int, perPage: Int, query: String?, gradoId: Int?, seccionId: Int?, bimesterId: Int?): Result<QuizzesPage> {
        return try {
            val response = api.getQuizzes(page = page, perPage = perPage, query = query, gradoId = gradoId, seccionId = seccionId, bimesterId = bimesterId)
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

    override suspend fun uploadAnswerKey(quizId: Int, fileName: String, mimeType: String, fileBytes: ByteArray): Result<AnswerKey> {
        return try {
            val requestBody: RequestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = api.uploadAnswerKey(quizId, part)
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

    override suspend fun listAnswerKeys(quizId: Int, page: Int, pageSize: Int): Result<AnswerKeysPage> {
        return try {
            val response = api.listAnswerKeys(quizId)
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

    override suspend fun getQuizAnswers(id: Int): Result<QuizAnswersPage> {
        return try {
            val response = api.getQuizAnswers(id)
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
        bimesterId: Int?,
        unidadId: Int?,
        gradoId: Int?,
        seccionId: Int?,
        fecha: String,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ): Result<Quiz> {
        return try {
            val request = CreateQuizRequest(
                title = title,
                bimester_id = bimesterId,
                unidad_id = unidadId,
                grado_id = gradoId,
                seccion_id = seccionId,
                fecha = fecha,
                num_questions = numQuestions,
                detalle = detalle,
                asignacion_id = asignacionId
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
        bimesterId: Int?,
        unidadId: Int?,
        gradoId: Int?,
        seccionId: Int?,
        fecha: String?,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ): Result<Quiz> {
        return try {
            val request = UpdateQuizRequest(
                title = title,
                bimester_id = bimesterId,
                unidad_id = unidadId,
                grado_id = gradoId,
                seccion_id = seccionId,
                fecha = fecha,
                num_questions = numQuestions,
                detalle = detalle,
                asignacion_id = asignacionId
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

    override suspend fun deleteLatestAnswerKey(id: Int): Result<Unit> {
        return try {
            val response = api.deleteLatestAnswerKey(id)
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

    override suspend fun updateAnswerKeys(id: Int, answers: List<UpdateAnswerItem>): Result<Unit> {
        return try {
            val response = api.updateAnswerKeys(id, UpdateAnswersRequest(answers = answers))
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

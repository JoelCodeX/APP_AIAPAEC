package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.models.AnswerKey
import com.jotadev.aiapaec.domain.models.AnswerKeysPage
import com.jotadev.aiapaec.domain.models.QuizAnswersPage

interface QuizzesRepository {
    suspend fun getQuizzes(page: Int, perPage: Int, query: String?, gradoId: Int?, seccionId: Int?, bimesterId: Int?, asignacionId: Int? = null): Result<QuizzesPage>
    suspend fun getQuiz(id: Int): Result<Quiz>
    suspend fun createQuiz(
        bimesterId: Int?,
        unidadId: Int?,
        gradoId: Int?,
        seccionId: Int?,
        weekId: Int?,
        fecha: String,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ): Result<Quiz>
    suspend fun updateQuiz(
        id: Int,
        bimesterId: Int?,
        unidadId: Int?,
        gradoId: Int?,
        seccionId: Int?,
        weekId: Int?,
        fecha: String?,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ): Result<Quiz>
    suspend fun deleteQuiz(id: Int): Result<Unit>

    // Answer key operations
    suspend fun uploadAnswerKey(
        quizId: Int,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray
    ): Result<AnswerKey>

    suspend fun listAnswerKeys(
        quizId: Int,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<AnswerKeysPage>

    suspend fun getQuizAnswers(id: Int): Result<QuizAnswersPage>

    suspend fun deleteLatestAnswerKey(id: Int): Result<Unit>

    suspend fun updateAnswerKeys(
        id: Int,
        answers: List<com.jotadev.aiapaec.data.api.UpdateAnswerItem>
    ): Result<Unit>

    suspend fun getQuizStatus(id: Int): Result<Map<String, com.jotadev.aiapaec.domain.models.StudentStatus>>
}

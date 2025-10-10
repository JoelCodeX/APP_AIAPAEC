package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.models.AnswerKey
import com.jotadev.aiapaec.domain.models.AnswerKeysPage
import com.jotadev.aiapaec.domain.models.QuizAnswersPage

interface QuizzesRepository {
    suspend fun getQuizzes(page: Int, perPage: Int, query: String?, classId: Int?, bimesterId: Int?): Result<QuizzesPage>
    suspend fun getQuiz(id: Int): Result<Quiz>
    suspend fun createQuiz(
        title: String,
        description: String?,
        classId: Int,
        bimesterId: Int,
        totalPoints: Double?,
        numQuestions: Int?,
        pointsPerQuestion: Double?,
        answerKeyFile: String?,
        keyVersion: String?
    ): Result<Quiz>
    suspend fun updateQuiz(
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
}
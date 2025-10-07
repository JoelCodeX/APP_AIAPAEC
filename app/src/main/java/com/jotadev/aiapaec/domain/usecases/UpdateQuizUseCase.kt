package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class UpdateQuizUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
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
        return repository.updateQuiz(id, title, description, classId, bimesterId, totalPoints, numQuestions, pointsPerQuestion, answerKeyFile, keyVersion)
    }
}
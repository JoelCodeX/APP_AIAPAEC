package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class DeleteQuizUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return repository.deleteQuiz(id)
    }
}
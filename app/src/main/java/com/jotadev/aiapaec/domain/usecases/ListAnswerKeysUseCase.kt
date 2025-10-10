package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.AnswerKeysPage
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class ListAnswerKeysUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
        quizId: Int,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<AnswerKeysPage> {
        return repository.listAnswerKeys(quizId, page, pageSize)
    }
}
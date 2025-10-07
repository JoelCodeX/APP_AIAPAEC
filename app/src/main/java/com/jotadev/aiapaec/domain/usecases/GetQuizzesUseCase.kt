package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class GetQuizzesUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(page: Int, perPage: Int, query: String?, classId: Int?, bimesterId: Int?): Result<QuizzesPage> {
        return repository.getQuizzes(page, perPage, query, classId, bimesterId)
    }
}
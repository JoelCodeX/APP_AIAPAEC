package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.QuizzesPage
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class GetQuizzesUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(page: Int, perPage: Int, query: String?, gradoId: Int?, seccionId: Int?, bimesterId: Int?, asignacionId: Int? = null): Result<QuizzesPage> {
        return repository.getQuizzes(page, perPage, query, gradoId, seccionId, bimesterId, asignacionId)
    }
}

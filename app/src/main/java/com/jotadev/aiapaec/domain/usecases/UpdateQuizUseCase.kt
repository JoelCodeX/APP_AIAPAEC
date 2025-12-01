package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class UpdateQuizUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
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
    ): Result<Quiz> {
        return repository.updateQuiz(id, bimesterId, unidadId, gradoId, seccionId, weekId, fecha, numQuestions, detalle, asignacionId)
    }
}

package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class CreateQuizUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
        bimesterId: Int?,
        unidadId: Int?,
        gradoId: Int?,
        seccionId: Int?,
        weekNumber: Int?,
        fecha: String,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?
    ): Result<Quiz> {
        return repository.createQuiz(bimesterId, unidadId, gradoId, seccionId, weekNumber, fecha, numQuestions, detalle, asignacionId)
    }
}

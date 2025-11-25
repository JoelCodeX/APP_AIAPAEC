package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class CreateQuizUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
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
        return repository.createQuiz(title, bimesterId, unidadId, gradoId, seccionId, fecha, numQuestions, detalle, asignacionId)
    }
}

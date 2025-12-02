package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.QuizDto
import com.jotadev.aiapaec.data.api.QuizzesPageDto
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage

fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id,
        bimesterId = bimester_id,
        unidadId = unidad_id,
        sedeId = sede_id,
        gradoId = grado_id,
        seccionId = seccion_id,
        weekId = week_id,
        weekNumber = week_number,
        fecha = fecha,
        numQuestions = num_questions,
        detalle = detalle,
        createdAt = created_at,
        updatedAt = updated_at,
        asignacionId = asignacion_id,
        gradoNombre = grado_nombre,
        seccionNombre = seccion_nombre,
        bimesterName = bimester_name
    )
}

fun QuizzesPageDto.toDomain(): QuizzesPage {
    return QuizzesPage(
        items = items.map { it.toDomain() },
        page = page,
        perPage = per_page,
        total = total,
        pages = pages
    )
}

package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.GradeDto
import com.jotadev.aiapaec.data.api.GradesPageDto
import com.jotadev.aiapaec.domain.models.Grade
import com.jotadev.aiapaec.domain.models.GradesPage

fun GradeDto.toDomain(): Grade {
    return Grade(
        id = id,
        nombre = nombre,
        nivel = nivel,
        descripcion = descripcion,
        studentCount = studentCount,
        sections = sections
    )
}

fun GradesPageDto.toDomain(): GradesPage {
    return GradesPage(
        items = items.map { it.toDomain() },
        page = page,
        perPage = per_page,
        total = total,
        pages = pages
    )
}


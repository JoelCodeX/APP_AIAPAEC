package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.ClassDto
import com.jotadev.aiapaec.data.api.ClassesPageDto
import com.jotadev.aiapaec.domain.models.SchoolClass
import com.jotadev.aiapaec.domain.models.ClassesPage

fun ClassDto.toDomain(): SchoolClass {
    return SchoolClass(
        id = this.id,
        name = this.name,
        level = this.level,
        studentCount = this.studentCount
    )
}

fun ClassesPageDto.toDomain(): ClassesPage {
    return ClassesPage(
        items = this.items.map { it.toDomain() },
        page = this.page,
        perPage = this.per_page,
        total = this.total,
        pages = this.pages
    )
}
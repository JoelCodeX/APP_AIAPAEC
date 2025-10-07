package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.BimesterDto
import com.jotadev.aiapaec.data.api.BimestersPageDto
import com.jotadev.aiapaec.domain.models.Bimester
import com.jotadev.aiapaec.domain.models.BimestersPage

fun BimesterDto.toDomain(): Bimester {
    return Bimester(
        id = this.id,
        name = this.name,
        startDate = this.start_date,
        endDate = this.end_date,
        academicYear = this.academic_year
    )
}

fun BimestersPageDto.toDomain(): BimestersPage {
    return BimestersPage(
        items = this.items.map { it.toDomain() },
        page = this.page,
        perPage = this.per_page,
        total = this.total,
        pages = this.pages
    )
}
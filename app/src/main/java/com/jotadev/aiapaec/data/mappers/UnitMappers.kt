package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.UnitDto
import com.jotadev.aiapaec.data.api.UnitsPageDto
import com.jotadev.aiapaec.domain.models.Unit
import com.jotadev.aiapaec.domain.models.UnitsPage

fun UnitDto.toDomain(): Unit {
    return Unit(
        id = id,
        bimesterId = bimester_id,
        unitNumber = unit_number,
        name = name,
        startDate = start_date,
        endDate = end_date
    )
}

fun UnitsPageDto.toDomain(): UnitsPage {
    return UnitsPage(
        items = items.map { it.toDomain() },
        page = page,
        perPage = per_page,
        total = total,
        pages = pages
    )
}


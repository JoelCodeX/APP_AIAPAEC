package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.WeekDto
import com.jotadev.aiapaec.data.api.WeeksPageDto
import com.jotadev.aiapaec.domain.models.Week
import com.jotadev.aiapaec.domain.models.WeeksPage

fun WeekDto.toDomain(): Week {
    return Week(
        id = id,
        unitId = unit_id,
        weekNumber = week_number,
        startDate = start_date,
        endDate = end_date
    )
}

fun WeeksPageDto.toDomain(): WeeksPage {
    return WeeksPage(
        items = items.map { it.toDomain() },
        page = page,
        perPage = per_page,
        total = total,
        pages = pages
    )
}


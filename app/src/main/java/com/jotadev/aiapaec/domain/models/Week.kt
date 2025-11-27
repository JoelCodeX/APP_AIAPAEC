package com.jotadev.aiapaec.domain.models

data class Week(
    val id: Int,
    val unitId: Int,
    val weekNumber: Int,
    val startDate: String?,
    val endDate: String?
)

data class WeeksPage(
    val items: List<Week>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)


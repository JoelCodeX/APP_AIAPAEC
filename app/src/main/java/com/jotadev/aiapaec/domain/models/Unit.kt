package com.jotadev.aiapaec.domain.models

data class Unit(
    val id: Int,
    val bimesterId: Int,
    val unitNumber: Int,
    val name: String?,
    val startDate: String?,
    val endDate: String?
)

data class UnitsPage(
    val items: List<Unit>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)


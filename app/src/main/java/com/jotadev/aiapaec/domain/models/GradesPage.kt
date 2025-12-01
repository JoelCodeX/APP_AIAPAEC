package com.jotadev.aiapaec.domain.models

data class GradesPage(
    val items: List<Grade>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)


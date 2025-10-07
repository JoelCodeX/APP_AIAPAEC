package com.jotadev.aiapaec.domain.models

data class Bimester(
    val id: Int,
    val name: String,
    val startDate: String,
    val endDate: String,
    val academicYear: Int
)
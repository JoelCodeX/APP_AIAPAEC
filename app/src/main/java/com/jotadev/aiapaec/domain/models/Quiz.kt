package com.jotadev.aiapaec.domain.models

data class Quiz(
    val id: Int,
    val bimesterId: Int?,
    val unidadId: Int?,
    val sedeId: Int?,
    val gradoId: Int?,
    val seccionId: Int?,
    val weekId: Int?,
    val weekNumber: Int?,
    val fecha: String,
    val numQuestions: Int?,
    val detalle: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val gradoNombre: String?,
    val seccionNombre: String?,
    val bimesterName: String?
)

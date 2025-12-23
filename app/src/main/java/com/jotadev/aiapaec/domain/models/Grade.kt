package com.jotadev.aiapaec.domain.models

import com.jotadev.aiapaec.data.api.SectionDto

data class Grade(
    val id: Int,
    val nombre: String,
    val nivel: String?,
    val descripcion: String?,
    val studentCount: Int = 0,
    val sections: List<SectionDto> = emptyList()
)


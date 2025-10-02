package com.jotadev.aiapaec.domain.models

data class StudentsPage(
    val items: List<Student>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)
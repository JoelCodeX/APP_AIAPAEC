package com.jotadev.aiapaec.domain.models

data class ClassesPage(
    val items: List<SchoolClass>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)
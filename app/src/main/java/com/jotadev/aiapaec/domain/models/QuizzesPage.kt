package com.jotadev.aiapaec.domain.models

data class QuizzesPage(
    val items: List<Quiz>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)
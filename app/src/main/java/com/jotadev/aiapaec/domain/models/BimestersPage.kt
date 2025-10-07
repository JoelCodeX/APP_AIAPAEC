package com.jotadev.aiapaec.domain.models

data class BimestersPage(
    val items: List<Bimester>,
    val page: Int,
    val perPage: Int,
    val total: Int,
    val pages: Int
)
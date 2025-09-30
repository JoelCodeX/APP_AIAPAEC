package com.jotadev.aiapaec.domain.models

data class LoginResult(
    val message: String,
    val token: String?,
    val user: User?
)
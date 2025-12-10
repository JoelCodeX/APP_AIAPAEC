package com.jotadev.aiapaec.domain.models

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val role: String,
    val status: String,
    val branchId: Int?,
    val branchName: String?,
    val lastLogin: String?,
    val createdAt: String,
    val updatedAt: String
)

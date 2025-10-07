package com.jotadev.aiapaec.domain.models

data class Quiz(
    val id: Int,
    val title: String,
    val description: String?,
    val classId: Int,
    val bimesterId: Int,
    val totalPoints: Double?,
    val numQuestions: Int?,
    val pointsPerQuestion: Double?,
    val answerKeyFile: String?,
    val keyVersion: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val className: String?,
    val bimesterName: String?
)
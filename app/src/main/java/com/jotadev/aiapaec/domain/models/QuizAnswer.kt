package com.jotadev.aiapaec.domain.models

data class QuizAnswer(
    val id: Int,
    val quizId: Int,
    val questionNumber: Int,
    val correctOption: String,
    val pointsValue: Double?,
    val createdAt: String?,
    val updatedAt: String?
)

data class QuizAnswersPage(
    val items: List<QuizAnswer>
)
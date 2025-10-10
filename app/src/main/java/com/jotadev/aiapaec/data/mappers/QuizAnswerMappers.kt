package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.QuizAnswerDto
import com.jotadev.aiapaec.data.api.QuizAnswersPageDto
import com.jotadev.aiapaec.domain.models.QuizAnswer
import com.jotadev.aiapaec.domain.models.QuizAnswersPage

fun QuizAnswerDto.toDomain(): QuizAnswer {
    return QuizAnswer(
        id = id,
        quizId = quiz_id,
        questionNumber = question_number,
        correctOption = correct_option,
        pointsValue = points_value,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

fun QuizAnswersPageDto.toDomain(): QuizAnswersPage {
    return QuizAnswersPage(
        items = items.map { it.toDomain() }
    )
}
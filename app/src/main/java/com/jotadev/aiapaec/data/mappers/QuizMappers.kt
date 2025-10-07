package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.QuizDto
import com.jotadev.aiapaec.data.api.QuizzesPageDto
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizzesPage

fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id,
        title = title,
        description = description,
        classId = class_id,
        bimesterId = bimester_id,
        totalPoints = total_points,
        numQuestions = num_questions,
        pointsPerQuestion = points_per_question,
        answerKeyFile = answer_key_file,
        keyVersion = key_version,
        createdAt = created_at,
        updatedAt = updated_at,
        className = class_name,
        bimesterName = bimester_name
    )
}

fun QuizzesPageDto.toDomain(): QuizzesPage {
    return QuizzesPage(
        items = items.map { it.toDomain() },
        page = page,
        perPage = per_page,
        total = total,
        pages = pages
    )
}
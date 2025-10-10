package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.AnswerKeyDto
import com.jotadev.aiapaec.data.api.AnswerKeysPageDto
import com.jotadev.aiapaec.domain.models.AnswerKey
import com.jotadev.aiapaec.domain.models.AnswerKeysPage

fun AnswerKeyDto.toDomain(): AnswerKey {
    return AnswerKey(
        id = id,
        quizId = quiz_id,
        version = version,
        filePath = file_path,
        parsedKeys = parsed_keys,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

fun AnswerKeysPageDto.toDomain(): AnswerKeysPage {
    return AnswerKeysPage(
        items = items.map { it.toDomain() }
    )
}
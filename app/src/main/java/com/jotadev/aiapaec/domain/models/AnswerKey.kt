package com.jotadev.aiapaec.domain.models

data class AnswerKey(
    val id: Int,
    val quizId: Int,
    val version: Int,
    val filePath: String,
    val parsedKeys: List<Map<String, Any>>?,
    val createdAt: String?,
    val updatedAt: String?
)
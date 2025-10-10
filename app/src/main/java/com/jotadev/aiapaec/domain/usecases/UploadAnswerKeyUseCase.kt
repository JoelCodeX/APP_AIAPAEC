package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.AnswerKey
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.QuizzesRepository

class UploadAnswerKeyUseCase(private val repository: QuizzesRepository) {
    suspend operator fun invoke(
        quizId: Int,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray
    ): Result<AnswerKey> {
        return repository.uploadAnswerKey(quizId, fileName, mimeType, fileBytes)
    }
}
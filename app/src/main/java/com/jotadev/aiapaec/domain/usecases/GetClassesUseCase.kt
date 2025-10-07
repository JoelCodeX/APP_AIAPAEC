package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.ClassesPage
import com.jotadev.aiapaec.domain.repository.ClassesRepository

class GetClassesUseCase(private val repository: ClassesRepository) {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        query: String?,
        level: String?
    ): Result<ClassesPage> {
        return repository.getClasses(page, perPage, query, level)
    }
}
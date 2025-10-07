package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.ClassesPage

interface ClassesRepository {
    suspend fun getClasses(
        page: Int = 1,
        perPage: Int = 20,
        query: String? = null,
        level: String? = null
    ): Result<ClassesPage>
}
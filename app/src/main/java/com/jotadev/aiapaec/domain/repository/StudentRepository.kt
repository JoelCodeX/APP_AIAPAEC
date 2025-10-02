package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.StudentsPage

interface StudentRepository {
    suspend fun getStudents(page: Int = 1, perPage: Int = 20, query: String? = null): Result<StudentsPage>
}
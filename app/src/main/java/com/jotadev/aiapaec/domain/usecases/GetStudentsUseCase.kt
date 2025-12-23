package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.StudentsPage
import com.jotadev.aiapaec.domain.repository.StudentRepository

class GetStudentsUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        query: String?,
        gradeId: Int? = null,
        sectionId: Int? = null,
        sortBy: String? = "id",
        order: String? = "asc"
    ): Result<StudentsPage> {
        return repository.getStudents(page, perPage, query, gradeId, sectionId, sortBy, order)
    }
}

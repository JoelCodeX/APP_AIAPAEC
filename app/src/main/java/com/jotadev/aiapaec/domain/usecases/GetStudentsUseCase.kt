package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.StudentsPage
import com.jotadev.aiapaec.domain.repository.StudentRepository

class GetStudentsUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(page: Int, perPage: Int, query: String?): Result<StudentsPage> {
        return repository.getStudents(page, perPage, query)
    }
}
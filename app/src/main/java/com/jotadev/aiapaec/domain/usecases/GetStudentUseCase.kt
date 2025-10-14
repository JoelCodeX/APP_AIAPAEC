package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.repository.StudentRepository

class GetStudentUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(id: Int): Result<Student> {
        return repository.getStudent(id)
    }
}
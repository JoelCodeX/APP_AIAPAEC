package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.StudentsPage
import com.jotadev.aiapaec.domain.models.Student

interface StudentRepository {
    suspend fun getStudents(
        page: Int = 1,
        perPage: Int = 20,
        query: String? = null,
        gradeId: Int? = null,
        sectionId: Int? = null,
        sortBy: String? = "id",
        order: String? = "asc"
    ): Result<StudentsPage>
    suspend fun getStudent(id: Int): Result<Student>
}

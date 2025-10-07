package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.StudentDto
import com.jotadev.aiapaec.data.api.StudentsPageDto
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.StudentsPage

fun StudentDto.toDomain(): Student {
    return Student(
        id = this.id,
        branchId = this.branch_id,
        firstName = this.first_name,
        lastName = this.last_name,
        email = this.email,
        phone = this.phone,
        dateOfBirth = this.date_of_birth,
        gender = this.gender,
        address = this.address,
        guardianName = this.guardian_name,
        enrollmentDate = this.enrollment_date,
        className = this.class_name,
        classId = this.class_id
    )
}

fun StudentsPageDto.toDomain(): StudentsPage {
    return StudentsPage(
        items = this.items.map { it.toDomain() },
        page = this.page,
        perPage = this.per_page,
        total = this.total,
        pages = this.pages
    )
}
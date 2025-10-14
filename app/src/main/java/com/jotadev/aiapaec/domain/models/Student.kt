package com.jotadev.aiapaec.domain.models

data class Student(
    val id: Int,
    val branchId: Int,
    val classId: Int?,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val religion: String?,
    val address: String?,
    val guardianName: String?,
    val enrollmentDate: String?,
    val className: String?,

)
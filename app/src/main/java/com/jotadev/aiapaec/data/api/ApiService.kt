package com.jotadev.aiapaec.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String? = null,
    val user: UserData? = null
)

data class UserData(
    val id: Int,
    val username: String,
    val full_name: String,
    val email: String,
    val role: String,
    val status: String,
    val branch_id: Int?,
    val last_login: String?,
    val created_at: String,
    val updated_at: String
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // STUDENTS LISTING (branch is enforced by backend via JWT user)
    @GET("students")
    suspend fun getStudents(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("q") query: String? = null
    ): Response<StudentsResponse>

    // CLASSES LISTING
    @GET("classes")
    suspend fun getClasses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("q") query: String? = null,
        @Query("level") level: String? = null
    ): Response<ClassesResponse>
}

// Generic API wrapper format: { success, message, data }
data class StudentsResponse(
    val success: Boolean,
    val message: String,
    val data: StudentsPageDto?
)

data class ClassesResponse(
    val success: Boolean,
    val message: String,
    val data: ClassesPageDto?
)

data class StudentsPageDto(
    val items: List<StudentDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class ClassesPageDto(
    val items: List<ClassDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class StudentDto(
    val id: Int,
    val branch_id: Int,
    @SerializedName("class_id") val class_id: Int?,
    val first_name: String,
    val last_name: String,
    val email: String?,
    val phone: String?,
    val date_of_birth: String?,
    val gender: String?,
    val address: String?,
    val guardian_name: String?,
    val enrollment_date: String?,
    val class_name: String?
)

data class ClassDto(
    val id: Int,
    val name: String,
    val level: String,
    @SerializedName("student_count") val studentCount: Int
)
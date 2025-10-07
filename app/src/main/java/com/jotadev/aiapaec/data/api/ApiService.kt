package com.jotadev.aiapaec.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
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

    // BIMESTERS LISTING
    @GET("bimesters")
    suspend fun getBimesters(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("q") query: String? = null,
        @Query("year") year: Int? = null
    ): Response<BimestersResponse>

    // QUIZZES
    @GET("quizzes")
    suspend fun getQuizzes(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("q") query: String? = null,
        @Query("class_id") classId: Int? = null,
        @Query("bimester_id") bimesterId: Int? = null
    ): Response<QuizzesListResponse>

    @GET("quizzes/{id}")
    suspend fun getQuiz(
        @Path("id") id: Int
    ): Response<QuizItemResponse>

    @POST("quizzes")
    suspend fun createQuiz(
        @Body request: CreateQuizRequest
    ): Response<QuizItemResponse>

    @PUT("quizzes/{id}")
    suspend fun updateQuiz(
        @Path("id") id: Int,
        @Body request: UpdateQuizRequest
    ): Response<QuizItemResponse>

    @DELETE("quizzes/{id}")
    suspend fun deleteQuiz(
        @Path("id") id: Int
    ): Response<ApiResponseNoData>
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

data class BimestersResponse(
    val success: Boolean,
    val message: String,
    val data: BimestersPageDto?
)

data class QuizzesListResponse(
    val success: Boolean,
    val message: String,
    val data: QuizzesPageDto?
)

data class QuizItemResponse(
    val success: Boolean,
    val message: String,
    val data: QuizDto?
)

data class ApiResponseNoData(
    val success: Boolean,
    val message: String
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

data class BimestersPageDto(
    val items: List<BimesterDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class QuizzesPageDto(
    val items: List<QuizDto>,
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

data class BimesterDto(
    val id: Int,
    val name: String,
    @SerializedName("start_date") val start_date: String,
    @SerializedName("end_date") val end_date: String,
    @SerializedName("academic_year") val academic_year: Int
)

data class QuizDto(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("class_id") val class_id: Int,
    @SerializedName("bimester_id") val bimester_id: Int,
    @SerializedName("total_points") val total_points: Double?,
    @SerializedName("num_questions") val num_questions: Int?,
    @SerializedName("points_per_question") val points_per_question: Double?,
    @SerializedName("answer_key_file") val answer_key_file: String?,
    @SerializedName("key_version") val key_version: String?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("class_name") val class_name: String?,
    @SerializedName("bimester_name") val bimester_name: String?
)

data class CreateQuizRequest(
    val title: String,
    val description: String?,
    val class_id: Int,
    val bimester_id: Int,
    val total_points: Double?,
    val num_questions: Int?,
    val points_per_question: Double?,
    val answer_key_file: String?,
    val key_version: String?
)

data class UpdateQuizRequest(
    val title: String?,
    val description: String?,
    val class_id: Int?,
    val bimester_id: Int?,
    val total_points: Double?,
    val num_questions: Int?,
    val points_per_question: Double?,
    val answer_key_file: String?,
    val key_version: String?
)
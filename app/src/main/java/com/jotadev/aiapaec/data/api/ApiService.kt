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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

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
    val branch_name: String?,
    val last_login: String?,
    val created_at: String,
    val updated_at: String
)

interface ApiService {
    @GET("auth/verify")
    suspend fun verifyToken(): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // STUDENTS LISTING (branch is enforced by backend via JWT user)
    @GET("students")
    suspend fun getStudents(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("q") query: String? = null,
        @Query("id") id: Int? = null,
        @Query("grade_id") gradeId: Int? = null,
        @Query("section_id") sectionId: Int? = null,
        @Query("sort_by") sortBy: String? = "id",
        @Query("order") order: String? = "asc"
    ): Response<StudentsResponse>

    // STUDENT DETAIL
    @GET("students/{id}")
    suspend fun getStudent(
        @Path("id") id: Int
    ): Response<StudentItemResponse>

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
        @Query("grado_id") gradoId: Int? = null,
        @Query("seccion_id") seccionId: Int? = null,
        @Query("bimester_id") bimesterId: Int? = null,
        @Query("asignacion_id") asignacionId: Int? = null
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

    @GET("units")
    suspend fun getUnits(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("bimester_id") bimesterId: Int? = null
    ): Response<UnitsResponse>

    @GET("weeks")
    suspend fun getWeeks(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("unit_id") unitId: Int? = null
    ): Response<WeeksResponse>
    
    // ANSWER KEYS
    @Multipart
    @POST("quizzes/{id}/answer_keys")
    suspend fun uploadAnswerKey(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<AnswerKeyItemResponse>

    @GET("quizzes/{id}/answer_keys")
    suspend fun listAnswerKeys(
        @Path("id") id: Int
    ): Response<AnswerKeysListResponse>

    @DELETE("quizzes/{id}/answer_keys")
    suspend fun deleteLatestAnswerKey(
        @Path("id") id: Int
    ): Response<ApiResponseNoData>

    @PUT("quizzes/{id}/answer_keys")
    suspend fun updateAnswerKeys(
        @Path("id") id: Int,
        @Body request: UpdateAnswersRequest
    ): Response<ApiResponseNoData>

    // QUIZ ANSWERS
    @GET("quizzes/{id}/answers")
    suspend fun getQuizAnswers(
        @Path("id") id: Int
    ): Response<QuizAnswersListResponse>

    // SCAN STATUS
    @GET("scan/status/{id}")
    suspend fun getQuizStatus(
        @Path("id") id: Int
    ): Response<Map<String, StudentStatusDto>>

    // GRADES
    @GET("grades")
    suspend fun getGrades(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("q") query: String? = null
    ): Response<GradesResponse>

    // BRANCH-SCOPED GRADES
    @GET("grades/by-branch")
    suspend fun getGradesByBranch(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100
    ): Response<GradesResponse>

    // SECTIONS
    @GET("sections")
    suspend fun getSections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("q") query: String? = null
    ): Response<SectionsResponse>

    // BRANCH-SCOPED SECTIONS
    @GET("sections/by-branch")
    suspend fun getSectionsByBranch(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("grade_id") gradeId: Int? = null
    ): Response<SectionsResponse>

    // WEEKLY FORMAT ASSIGNMENTS (CRUD)
    @GET("weekly-assignments")
    suspend fun getWeeklyAssignments(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<WeeklyAssignmentsResponse>

    @POST("weekly-assignments")
    suspend fun createWeeklyAssignment(
        @Body request: CreateWeeklyAssignmentRequest
    ): Response<WeeklyAssignmentItemResponse>

    @PUT("weekly-assignments/{id}")
    suspend fun updateWeeklyAssignment(
        @Path("id") id: Int,
        @Body request: UpdateWeeklyAssignmentRequest
    ): Response<WeeklyAssignmentItemResponse>

    @DELETE("weekly-assignments/{id}")
    suspend fun deleteWeeklyAssignment(
        @Path("id") id: Int
    ): Response<ApiResponseNoData>
}

// Generic API wrapper format: { success, message, data }
data class StudentsResponse(
    val success: Boolean,
    val message: String,
    val data: StudentsPageDto?
)

data class StudentItemResponse(
    val success: Boolean,
    val message: String,
    val data: StudentDto?
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

data class UpdateAnswersRequest(
    val answers: List<UpdateAnswerItem>
)

// WEEKLY ASSIGNMENTS DTOS
data class WeeklyAssignmentsResponse(
    val success: Boolean,
    val message: String,
    val data: WeeklyAssignmentsPageDto?
)

data class UnitsResponse(
    val success: Boolean,
    val message: String,
    val data: UnitsPageDto?
)

data class WeeksResponse(
    val success: Boolean,
    val message: String,
    val data: WeeksPageDto?
)

data class WeeklyAssignmentItemResponse(
    val success: Boolean,
    val message: String,
    val data: WeeklyAssignmentDto?
)

data class WeeklyAssignmentsPageDto(
    val items: List<WeeklyAssignmentDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class WeeklyAssignmentDto(
    val id: Int,
    @SerializedName("sede_id") val sede_id: Int?,
    @SerializedName("grado_id") val grado_id: Int?,
    @SerializedName("grado_nombre") val grado_nombre: String?,
    @SerializedName("seccion_id") val seccion_id: Int?,
    @SerializedName("seccion_nombre") val seccion_nombre: String?,
    @SerializedName("numero_preguntas") val numero_preguntas: Int,
    @SerializedName("formato_id") val formato_id: Int?,
    @SerializedName("formato_nombre") val formato_nombre: String?,
    @SerializedName("session_id") val session_id: Int?,
    @SerializedName("puntaje") val puntaje: Double?
)

data class CreateWeeklyAssignmentRequest(
    val grade: String,
    val section: String,
    @SerializedName("num_questions") val num_questions: Int,
    @SerializedName("format_type") val format_type: String,
    @SerializedName("score_format") val score_format: String
)

data class UpdateWeeklyAssignmentRequest(
    val grade: String,
    val section: String,
    @SerializedName("num_questions") val num_questions: Int,
    @SerializedName("format_type") val format_type: String,
    @SerializedName("score_format") val score_format: String
)

data class UpdateAnswerItem(
    @SerializedName("question_number") val question_number: Int,
    @SerializedName("correct_option") val correct_option: String,
    @SerializedName("points_value") val points_value: Double?
)

data class AnswerKeysListResponse(
    val success: Boolean,
    val message: String,
    val data: AnswerKeysPageDto?
)

data class AnswerKeyItemResponse(
    val success: Boolean,
    val message: String,
    val data: AnswerKeyDto?
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

data class AnswerKeysPageDto(
    val items: List<AnswerKeyDto>
)

data class UnitsPageDto(
    val items: List<UnitDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class WeeksPageDto(
    val items: List<WeekDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class QuizAnswersListResponse(
    val success: Boolean,
    val message: String,
    val data: QuizAnswersPageDto?
)

data class QuizAnswersPageDto(
    val items: List<QuizAnswerDto>
)

// Grades/Sections DTOs
data class GradesResponse(
    val success: Boolean,
    val message: String,
    val data: GradesPageDto?
)

data class SectionsResponse(
    val success: Boolean,
    val message: String,
    val data: SectionsPageDto?
)

data class GradesPageDto(
    val items: List<GradeDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class SectionsPageDto(
    val items: List<SectionDto>,
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int
)

data class GradeDto(
    val id: Int,
    val nombre: String,
    val nivel: String?,
    val descripcion: String?,
    @SerializedName("student_count") val studentCount: Int = 0,
    val sections: List<SectionDto> = emptyList()
)

data class SectionDto(
    val id: Int,
    val nombre: String,
    @SerializedName("grade_id") val gradeId: Int? = null,
    val turno: String?,
    val capacidad: Int?,
    @SerializedName("student_count") val studentCount: Int = 0
)

data class StudentDto(
    val id: Int,
    val branch_id: Int,
    @SerializedName("class_id") val class_id: Int?,
    val first_name: String,
    val last_name: String,
    val email: String?,
    @SerializedName("mobileno") val phone: String?,
    val date_of_birth: String?,
    val gender: String?,
    val religion: String?,
    val address: String?,
    val guardian_name: String?,
    @SerializedName("admission_date") val enrollment_date: String?,
    val class_name: String?
)

data class ClassDto(
    val id: Int,
    val name: String,
    val grade: String?,
    val section: String?,
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

data class UnitDto(
    val id: Int,
    @SerializedName("bimester_id") val bimester_id: Int,
    @SerializedName("unit_number") val unit_number: Int,
    val name: String?,
    @SerializedName("start_date") val start_date: String?,
    @SerializedName("end_date") val end_date: String?
)

data class WeekDto(
    val id: Int,
    @SerializedName("unit_id") val unit_id: Int,
    @SerializedName("week_number") val week_number: Int,
    @SerializedName("start_date") val start_date: String?,
    @SerializedName("end_date") val end_date: String?
)

data class QuizDto(
    val id: Int,
    // TITLE REMOVED
    @SerializedName("bimester_id") val bimester_id: Int?,
    @SerializedName("unidad_id") val unidad_id: Int?,
    @SerializedName("sede_id") val sede_id: Int?,
    @SerializedName("grado_id") val grado_id: Int?,
    @SerializedName("seccion_id") val seccion_id: Int?,
    @SerializedName("week_id") val week_id: Int?,
    @SerializedName("week_number") val week_number: Int?,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("num_questions") val num_questions: Int?,
    @SerializedName("detalle") val detalle: String?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("asignacion_id") val asignacion_id: Int?,
    @SerializedName("grado_nombre") val grado_nombre: String?,
    @SerializedName("seccion_nombre") val seccion_nombre: String?,
    @SerializedName("bimester_name") val bimester_name: String?,
    @SerializedName("scanned_count") val scanned_count: Int? = null,
    @SerializedName("total_count") val total_count: Int? = null
)

data class CreateQuizRequest(
    // TITLE REMOVED
    @SerializedName("bimester_id") val bimester_id: Int?,
    @SerializedName("unidad_id") val unidad_id: Int?,
    @SerializedName("grado_id") val grado_id: Int?,
    @SerializedName("seccion_id") val seccion_id: Int?,
    @SerializedName("week_id") val week_id: Int?,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("num_questions") val num_questions: Int?,
    @SerializedName("detalle") val detalle: String?,
    @SerializedName("asignacion_id") val asignacion_id: Int?
)

data class UpdateQuizRequest(
    // TITLE REMOVED
    @SerializedName("bimester_id") val bimester_id: Int?,
    @SerializedName("unidad_id") val unidad_id: Int?,
    @SerializedName("grado_id") val grado_id: Int?,
    @SerializedName("seccion_id") val seccion_id: Int?,
    @SerializedName("week_id") val week_id: Int?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("num_questions") val num_questions: Int?,
    @SerializedName("detalle") val detalle: String?,
    @SerializedName("asignacion_id") val asignacion_id: Int?
)

data class AnswerKeyDto(
    val id: Int,
    @SerializedName("quiz_id") val quiz_id: Int,
    val version: Int,
    @SerializedName("file_path") val file_path: String,
    @SerializedName("parsed_keys") val parsed_keys: List<Map<String, Any>>?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?
)

data class QuizAnswerDto(
    val id: Int,
    @SerializedName("quiz_id") val quiz_id: Int,
    @SerializedName("question_number") val question_number: Int,
    @SerializedName("correct_option") val correct_option: String,
    @SerializedName("points_value") val points_value: Double?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?
)

data class StudentStatusDto(
    val status: String,
    @SerializedName("run_id") val run_id: String?,
    val score: Double?
)

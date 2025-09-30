package com.jotadev.aiapaec.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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
}
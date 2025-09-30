package com.jotadev.aiapaec.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<ApiResponse<User>>
    
    @GET("user/perfil")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<ApiResponse<User>>
    
    @GET("status")
    suspend fun getStatus(): Response<ApiResponse<String>>
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val branchId: String
)
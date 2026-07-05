package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): AuthResponse
}

data class LoginRequest(val email: String, val password: String)
data class SignUpRequest(val email: String, val password: String, val nickname: String)
data class AuthResponse(val id: String, val email: String, val nickname: String)

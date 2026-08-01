package com.phoneshim.android.auth

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/google")
    suspend fun loginWithGoogle(
        @Body request: SocialLoginRequest,
    ): ApiResponse<SocialLoginResponseDto>

    @POST("api/auth/kakao")
    suspend fun loginWithKakao(
        @Body request: SocialLoginRequest,
    ): ApiResponse<SocialLoginResponseDto>
}

data class SocialLoginRequest(
    val accessToken: String,
)

data class SocialLoginResponseDto(
    val accessToken: String,
    val isNewUser: Boolean,
)

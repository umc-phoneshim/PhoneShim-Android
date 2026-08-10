package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.data.api.dto.LoginResponseDto
import com.phoneshim.android.data.api.dto.SocialLoginRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    // AuthInterceptor가 이 내부 헤더를 제거하면서 JWT 주입도 생략한다.
    @Headers("X-No-Authentication: true")
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: SocialLoginRequest): ApiResponse<LoginResponseDto>

    @Headers("X-No-Authentication: true")
    @POST("api/auth/kakao")
    suspend fun loginWithKakao(@Body request: SocialLoginRequest): ApiResponse<LoginResponseDto>
}

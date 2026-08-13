package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.data.api.dto.GoogleLoginRequest
import com.phoneshim.android.data.api.dto.KakaoLoginRequest
import com.phoneshim.android.data.api.dto.LoginResponseDto
import com.phoneshim.android.data.api.dto.LinkedAccountResponseDto
import com.phoneshim.android.data.api.dto.ProviderTokenRequest
import com.phoneshim.android.data.api.dto.RecoverWithdrawalResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    // AuthInterceptor가 이 내부 헤더를 제거하면서 JWT 주입도 생략한다.
    @Headers("X-No-Authentication: true")
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): ApiResponse<LoginResponseDto>

    @Headers("X-No-Authentication: true")
    @POST("api/auth/kakao")
    suspend fun loginWithKakao(@Body request: KakaoLoginRequest): ApiResponse<LoginResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @Headers("X-No-Authentication: true")
    @POST("api/auth/recover-withdrawal")
    suspend fun recoverWithdrawal(
        @Body request: ProviderTokenRequest,
    ): ApiResponse<RecoverWithdrawalResponseDto>

    @POST("api/auth/link-account")
    suspend fun linkAccount(
        @Body request: ProviderTokenRequest,
    ): ApiResponse<LinkedAccountResponseDto>
}

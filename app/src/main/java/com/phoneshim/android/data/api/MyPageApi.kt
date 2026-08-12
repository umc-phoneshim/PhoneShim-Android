package com.phoneshim.android.data.api

import com.phoneshim.android.data.api.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

/**
 * User 도메인 API. 백엔드 src/domains/user, src/domains/auth 구현을 직접 확인해 맞췄습니다.
 *
 * 탈퇴는 명세서 엑셀에 DELETE /api/users/me 로 적혀 있으나
 * 실제 구현은 authController 의 DELETE /withdraw 입니다.
 */
interface MyPageApi {

    /** userRouter: GET /me — 구현완료 */
    @GET("api/users/me")
    suspend fun getMyInfo(): ApiResponse<UserResponse>

    /** userRouter: PATCH /me — 구현완료. 명세서엔 "예정"으로 적혀 있으나 실제로는 동작합니다. */
    @PATCH("api/users/me")
    suspend fun updateMyInfo(@Body request: UpdateUserRequest): ApiResponse<UserResponse>

    /**
     * authController: DELETE /withdraw — 구현완료.
     * 204 가 아니라 200 + body 를 반환하며, 계정은 14일 유예(WITHDRAWAL_PENDING) 상태가 됩니다.
     */
    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): ApiResponse<WithdrawResponse>
}

/**
 * userRepository.getUserByUserId 의 select 절과 일치합니다.
 * id 와 status 는 응답에 포함되지 않습니다.
 *
 * Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어옵니다.
 * 그래서 전부 nullable 로 받고 도메인 변환에서 보정합니다.
 */
data class UserResponse(
    val email: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val motivation: String? = null,
    /** MALE / FEMALE */
    val gender: String? = null,
    /** TEENS / TWENTIES / THIRTIES / FORTIES / FIFTIES_PLUS */
    val ageGroup: String? = null,
)

/** 보내지 않은(null) 필드는 서버에서 변경하지 않습니다. motivation 은 최대 100자. */
data class UpdateUserRequest(
    val name: String? = null,
    val motivation: String? = null,
)

/** withdrawUserService 반환값. withdrawalRequestedAt 은 ISO 8601 문자열입니다. */
data class WithdrawResponse(
    val status: String? = null,
    val withdrawalRequestedAt: String? = null,
)

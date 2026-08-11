package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

/**
 * User 도메인 API. 백엔드 src/domains/user, src/domains/auth 구현 기준입니다.
 *
 * 탈퇴는 명세서 엑셀에 DELETE /api/users/me 로 적혀 있으나
 * 실제 서버 구현이 DELETE /api/auth/withdraw 입니다.
 */
interface MyPageApi {

    /** userRouter: GET /me — 구현완료 */
    @GET("api/users/me")
    suspend fun getMyInfo(): ApiResponse<UserResponse>

    /** userRouter: PATCH /me — 구현완료. name/motivation 수정. */
    @PATCH("api/users/me")
    suspend fun updateMyInfo(@Body request: UpdateUserRequest): ApiResponse<UserResponse>

    /** userRouter: PATCH /me/onboarding — 구현완료. 온보딩 담당 영역이라 여기서는 쓰지 않습니다. */
    @PATCH("api/users/me/onboarding")
    suspend fun updateGenderAge(@Body request: UpdateGenderAgeRequest): ApiResponse<UserResponse>

    /**
     * authController: DELETE /withdraw — 구현완료.
     * 204 가 아니라 200 + body 를 반환하고, 계정은 14일 유예(WITHDRAWAL_PENDING) 상태가 됩니다.
     */
    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): ApiResponse<WithdrawResponse>
}

/**
 * userRepository.getUserByUserId 의 select 절과 정확히 일치합니다.
 * id 와 status 는 응답에 포함되지 않습니다.
 */
data class UserResponse(
    val email: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val motivation: String? = null,
    val gender: String? = null,
    val ageGroup: String? = null,
)

/** 보내지 않은(null) 필드는 서버에서 변경하지 않습니다. motivation 은 최대 100자. */
data class UpdateUserRequest(
    val name: String? = null,
    val motivation: String? = null,
)

/** 온보딩 전용. gender: MALE|FEMALE, ageGroup: TEENS|TWENTIES|THIRTIES|FORTIES|FIFTIES_PLUS */
data class UpdateGenderAgeRequest(
    val gender: String,
    val ageGroup: String,
)

/** withdrawUserService 반환값. withdrawalRequestedAt 은 ISO 8601 문자열입니다. */
data class WithdrawResponse(
    val status: String? = null,
    val withdrawalRequestedAt: String? = null,
)

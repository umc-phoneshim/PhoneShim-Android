package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

/**
 * User 도메인 API. 명세서 4_Auth_User 기준.
 *
 * 탈퇴는 명세서 본문에는 DELETE /api/users/me 로 적혀 있으나
 * 실제 서버 구현이 DELETE /api/auth/withdraw 라 후자를 사용합니다.
 */
interface MyPageApi {

    /** 상태: 구현완료 */
    @GET("api/users/me")
    suspend fun getMyInfo(): ApiEnvelope<UserResponse>

    /** 상태: 예정. 이름/다짐 문구 수정. */
    @PATCH("api/users/me")
    suspend fun updateMyInfo(@Body request: UpdateUserRequest): ApiEnvelope<UserResponse>

    /** 상태: 구현완료. 즉시 삭제가 아니라 14일 유예(WITHDRAWAL_PENDING) 상태로 전환됩니다. */
    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): ApiEnvelope<WithdrawResponse>
}

data class UserResponse(
    val id: String? = null,
    val email: String,
    val name: String,
    val profileImage: String? = null,
    val motivation: String? = null,
    val status: String? = null,
)

/** 보내지 않은(null) 필드는 서버에서 변경하지 않습니다. */
data class UpdateUserRequest(
    val name: String? = null,
    val motivation: String? = null,
)

data class WithdrawResponse(
    val status: String? = null,
    val recoverableUntil: String? = null,
)

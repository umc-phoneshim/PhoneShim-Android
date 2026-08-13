package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.WithdrawalResult

interface MyPageRepository {
    suspend fun getMyInfo(): Result<User>

    /** 이름/다짐 문구 수정. null 인 항목은 변경하지 않습니다. */
    suspend fun updateMyInfo(name: String? = null, motivation: String? = null): Result<User>

    /** 성별·연령대를 User 서버 원본에 저장합니다. */
    suspend fun updateUserProfile(gender: String, ageGroup: String): Result<User>

    /** 탈퇴 요청. 즉시 삭제가 아니라 14일 유예 상태로 전환됩니다. */
    suspend fun withdraw(): Result<WithdrawalResult>
}

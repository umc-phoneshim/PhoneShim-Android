package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.data.api.UpdateUserRequest
import com.phoneshim.android.data.api.UserResponse
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
) : MyPageRepository {

    override suspend fun getMyInfo(): Result<User> = runCatchingApi {
        myPageApi.getMyInfo().unwrap().toDomain()
    }

    override suspend fun updateMyInfo(name: String?, motivation: String?): Result<User> =
        runCatchingApi {
            myPageApi.updateMyInfo(UpdateUserRequest(name = name, motivation = motivation))
                .unwrap()
                .toDomain()
        }

    override suspend fun withdraw(): Result<WithdrawalResult> = runCatchingApi {
        val response = myPageApi.withdraw().unwrap()
        WithdrawalResult(
            status = UserStatus.from(response.status).takeIf { it != UserStatus.UNKNOWN }
                ?: UserStatus.WITHDRAWAL_PENDING,
            withdrawalRequestedAt = response.withdrawalRequestedAt,
        )
    }
}

/**
 * 서버의 name 필드를 도메인 모델의 nickname 으로 매핑합니다.
 *
 * Gson 은 Kotlin 기본값을 적용하지 않아 응답에 없는 필드가 null 로 들어올 수 있어
 * 모든 필드를 nullable 로 받고 여기서 보정합니다.
 */
private fun UserResponse.toDomain(): User = User(
    email = email.orEmpty(),
    nickname = name.orEmpty(),
    profileImage = profileImage,
    motivation = motivation,
    gender = gender,
    ageGroup = ageGroup,
)

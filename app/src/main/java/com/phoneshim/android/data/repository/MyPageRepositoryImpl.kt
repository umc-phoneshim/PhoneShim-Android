package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.data.api.UpdateUserRequest
import com.phoneshim.android.data.api.UserResponse
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
    private val apiCallExecutor: ApiCallExecutor,
) : MyPageRepository {

    override suspend fun getMyInfo(): Result<User> =
        apiCallExecutor.executeAsResult { myPageApi.getMyInfo() }.map(UserResponse::toDomain)

    override suspend fun updateMyInfo(name: String?, motivation: String?): Result<User> =
        apiCallExecutor.executeAsResult {
            myPageApi.updateMyInfo(UpdateUserRequest(name = name, motivation = motivation))
        }.map(UserResponse::toDomain)

    override suspend fun withdraw(): Result<WithdrawalResult> =
        apiCallExecutor.executeAsResult { myPageApi.withdraw() }.map { response ->
            WithdrawalResult(
                status = UserStatus.from(response.status).takeIf { it != UserStatus.UNKNOWN }
                    ?: UserStatus.WITHDRAWAL_PENDING,
                recoverableUntil = response.recoverableUntil,
            )
        }
}

/** 명세의 name 필드를 도메인 모델의 nickname 으로 매핑합니다. */
private fun UserResponse.toDomain(): User = User(
    id = id.orEmpty(),
    email = email,
    nickname = name,
    profileImage = profileImage,
    motivation = motivation,
    status = UserStatus.from(status).takeIf { it != UserStatus.UNKNOWN } ?: UserStatus.ACTIVE,
)

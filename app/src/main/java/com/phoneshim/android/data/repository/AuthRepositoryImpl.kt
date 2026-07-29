package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.LoginRequest
import com.phoneshim.android.data.api.SignUpRequest
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = authApi.login(LoginRequest(email, password))
        // 명세상 사용자 이름 필드는 name 입니다. Auth 도메인 DTO 정리 시 함께 맞춰 주세요.
        User(id = response.id, email = response.email, name = response.nickname)
    }

    override suspend fun signUp(email: String, password: String, nickname: String): Result<User> = runCatching {
        val response = authApi.signUp(SignUpRequest(email, password, nickname))
        // 명세상 사용자 이름 필드는 name 입니다. Auth 도메인 DTO 정리 시 함께 맞춰 주세요.
        User(id = response.id, email = response.email, name = response.nickname)
    }
}

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
        User(id = response.id, email = response.email, nickname = response.nickname)
    }

    override suspend fun signUp(email: String, password: String, nickname: String): Result<User> = runCatching {
        val response = authApi.signUp(SignUpRequest(email, password, nickname))
        User(id = response.id, email = response.email, nickname = response.nickname)
    }
}

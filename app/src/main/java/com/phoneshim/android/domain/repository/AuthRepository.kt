package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, nickname: String): Result<User>
}

package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.User

interface MyPageRepository {
    suspend fun getMyInfo(): Result<User>
    suspend fun withdraw(): Result<Unit>
}

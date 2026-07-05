package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.repository.MyPageRepository
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
) : MyPageRepository {
    override suspend fun getMyInfo(): Result<User> = runCatching {
        val response = myPageApi.getMyInfo()
        User(id = response.id, email = response.email, nickname = response.nickname)
    }

    override suspend fun withdraw(): Result<Unit> = runCatching {
        myPageApi.withdraw()
    }
}

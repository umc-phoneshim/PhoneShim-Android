package com.phoneshim.android.data.api

import retrofit2.http.DELETE
import retrofit2.http.GET

interface MyPageApi {
    @GET("users/me")
    suspend fun getMyInfo(): AuthResponse

    @DELETE("users/me")
    suspend fun withdraw()
}

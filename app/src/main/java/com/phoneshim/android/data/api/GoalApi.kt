package com.phoneshim.android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GoalApi {
    @GET("goals/me")
    suspend fun getGoal(): GoalResponse?

    @POST("goals")
    suspend fun saveGoal(@Body request: GoalResponse)
}

data class GoalResponse(
    val id: String,
    val targetPackageNames: List<String>,
    val dailyUsageLimitMinutes: Int,
    val accessCountLimit: Int,
    val description: String,
)

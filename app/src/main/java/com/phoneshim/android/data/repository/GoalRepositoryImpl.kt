package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.GoalResponse
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApi: GoalApi,
) : GoalRepository {
    override suspend fun getGoal(): Result<Goal?> = runCatching {
        goalApi.getGoal()?.toDomain()
    }

    override suspend fun saveGoal(goal: Goal): Result<Unit> = runCatching {
        goalApi.saveGoal(
            GoalResponse(
                id = goal.id,
                targetPackageNames = goal.targetApps.map { it.packageName },
                dailyUsageLimitMinutes = goal.dailyUsageLimitMinutes,
                accessCountLimit = goal.accessCountLimit,
                description = goal.description,
            ),
        )
    }

    private fun GoalResponse.toDomain(): Goal = Goal(
        id = id,
        targetApps = targetPackageNames.map { AppUsage(packageName = it, appName = it, usageMinutes = 0) },
        dailyUsageLimitMinutes = dailyUsageLimitMinutes,
        accessCountLimit = accessCountLimit,
        description = description,
    )
}

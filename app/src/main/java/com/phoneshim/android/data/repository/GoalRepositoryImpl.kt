package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.GoalResponse
import com.phoneshim.android.domain.model.AppGoal
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
        // TODO: 서버 goal 계약 확정 시 앱별 시간/성별/나이까지 반영 (현재 GoalResponse는 스칼라 구조)
        goalApi.saveGoal(
            GoalResponse(
                id = goal.id.orEmpty(),
                targetPackageNames = goal.apps.map { it.packageName },
                dailyUsageLimitMinutes = goal.dailyGoalMinutes,
                accessCountLimit = goal.apps.count { it.accessLimited },
                description = "",
            ),
        )
    }

    private fun GoalResponse.toDomain(): Goal = Goal(
        id = id,
        dailyGoalMinutes = dailyUsageLimitMinutes,
        apps = targetPackageNames.map {
            AppGoal(packageName = it, appName = it, goalMinutes = 0, accessLimited = false)
        },
    )
}

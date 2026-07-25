package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.GoalResponse
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApi: GoalApi,
    private val goalDao: GoalDao,
) : GoalRepository {
    override suspend fun getGoal(): Result<Goal?> = runCatching {
        // 서버(원본) 우선, 네트워크 실패/오프라인이면 로컬 캐시로 폴백.
        runCatching { goalApi.getGoal()?.toDomain() }.getOrNull() ?: getLocalGoal()
    }

    // 차단 엔진 캐시(phone_goal_cache/app_goal_cache)에서 목표를 복원. 저장된 게 없으면 null.
    // 성별/나이는 캐시에 없어(엔진 미사용) null로 둔다 — 메인은 목표 시간/설정여부만 필요.
    private suspend fun getLocalGoal(): Goal? {
        val phone = goalDao.getPhoneGoal()
        val apps = goalDao.getAppGoals()
        if (phone == null && apps.isEmpty()) return null
        return Goal(
            dailyGoalMinutes = phone?.goalMinutes ?: 0,
            blockAfterGoal = phone?.limitEnabled ?: false,
            apps = apps.map {
                AppGoal(
                    packageName = it.packageName,
                    appName = it.appLabel,
                    goalMinutes = it.goalMinutes,
                    accessLimited = it.limitEnabled,
                )
            },
        )
    }

    override suspend fun saveGoal(goal: Goal): Result<Unit> = runCatching {
        // 1) 차단 엔진이 오프라인에서 읽는 로컬 캐시에 먼저 반영한다.
        //    엔진 판정의 실제 소스는 이 캐시(app_goal_cache / phone_goal_cache)다.
        goalDao.upsertPhoneGoal(
            PhoneGoalEntity(
                goalMinutes = goal.dailyGoalMinutes,
                limitEnabled = goal.blockAfterGoal,
            ),
        )
        // 온보딩 저장 = 주의앱 목록 전체 교체(재설정 시 해제된 앱 제거).
        goalDao.clearAppGoals()
        goalDao.upsertAppGoals(
            goal.apps.map { app ->
                AppGoalEntity(
                    packageName = app.packageName,
                    appLabel = app.appName,
                    goalMinutes = app.goalMinutes,
                    limitEnabled = app.accessLimited,
                )
            },
        )

        // 2) 서버 동기화(원본)는 best-effort. 아직 실서버 미연동이라
        //    네트워크 실패가 로컬 저장(엔진 동작)을 막지 않도록 분리한다.
        //    TODO: 실서버 계약 확정 시 remote-first + 성공 시 로컬 미러링으로 전환.
        runCatching {
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
        Unit
    }

    private fun GoalResponse.toDomain(): Goal = Goal(
        id = id,
        dailyGoalMinutes = dailyUsageLimitMinutes,
        apps = targetPackageNames.map {
            AppGoal(packageName = it, appName = it, goalMinutes = 0, accessLimited = false)
        },
    )
}

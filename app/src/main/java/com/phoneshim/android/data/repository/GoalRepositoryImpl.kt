package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.GoalResponse
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity
import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApi: GoalApi,
    private val goalDao: GoalDao,
    private val userProfileDao: UserProfileDao,
) : GoalRepository {
    override suspend fun getGoal(): Result<Goal?> = runCatching {
        // 서버(원본) 우선, 네트워크 실패/오프라인이면 로컬 캐시로 폴백.
        runCatching { goalApi.getGoal()?.toDomain() }.getOrNull() ?: getLocalGoal()
    }

    // 차단 엔진 캐시(phone_goal_cache/app_goal_cache)에서 목표를 복원. 저장된 게 없으면 null.
    // 성별/나이는 엔진이 안 쓰므로 user_profile_cache에 따로 두고 여기서 합쳐 돌려준다.
    private suspend fun getLocalGoal(): Goal? {
        val phone = goalDao.getPhoneGoal()
        val apps = goalDao.getAppGoals()
        if (phone == null && apps.isEmpty()) return null
        val profile = userProfileDao.getProfile()
        return Goal(
            gender = profile?.gender,
            ageGroup = profile?.ageGroup,
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
        // 성별·나이는 엔진 판정에 쓰이지 않지만, 온보딩 재진입 시 복원해야 하므로 함께 남긴다.
        // 서버 계약에 프로필 필드가 없어 아직 로컬이 유일한 저장처다.
        userProfileDao.upsertProfile(
            UserProfileEntity(
                gender = goal.gender,
                ageGroup = goal.ageGroup,
            ),
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

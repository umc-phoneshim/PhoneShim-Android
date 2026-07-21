package com.phoneshim.android.blocking.policy

import com.phoneshim.android.data.database.dao.GoalDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room 기반 정책 provider.
 *
 * 비어 있으면 [StubBlockingPolicyProvider] 값으로 폴백해 엔진 테스트가 가능하게 한다.
 * 온보딩 저장이 붙으면 자연스럽게 실제 값이 우선한다. 폴백은 그때 제거해도 됨.
 */
@Singleton
class RoomBlockingPolicyProvider @Inject constructor(
    private val goalDao: GoalDao,
    private val fallback: StubBlockingPolicyProvider,
) : BlockingPolicyProvider {

    override suspend fun phoneGoalMinutes(): Int? =
        goalDao.getPhoneGoal()?.goalMinutes ?: fallback.phoneGoalMinutes()

    override suspend fun phoneLimitEnabled(): Boolean =
        goalDao.getPhoneGoal()?.limitEnabled ?: fallback.phoneLimitEnabled()

    override suspend fun watchedApps(): List<AppBlockingPolicy> {
        val rows = goalDao.getAppGoals()
        if (rows.isEmpty()) return fallback.watchedApps()
        return rows.map {
            AppBlockingPolicy(
                packageName = it.packageName,
                appLabel = it.appLabel,
                goalMinutes = it.goalMinutes,
                limitEnabled = it.limitEnabled,
            )
        }
    }
}

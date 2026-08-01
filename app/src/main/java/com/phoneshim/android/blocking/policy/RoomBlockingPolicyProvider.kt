package com.phoneshim.android.blocking.policy

import com.phoneshim.android.data.database.dao.GoalDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room(Goal 테이블) 기반 정책 provider.
 *
 * 목표가 저장돼 있지 않으면(테이블 비어 있음) "목표 없음 = 차단 없음"으로 동작한다.
 *   - phoneGoal() == null          → 전체폰 쿼터 분기 skip
 *   - watchedApps().isEmpty()      → 주의앱 없음 → Allow
 * 온보딩/설정 저장이 붙으면 그 값이 그대로 판정에 쓰인다.
 *
 * (수동으로 stub 값으로 엔진을 돌려보고 싶으면 BlockingPolicyBindModule 에서
 *  bindPolicyProvider 를 StubBlockingPolicyProvider 로 바꿔 끼운다. 자동 폴백은 두지 않는다 —
 *  빈 상태에서 몰래 stub 목표로 차단돼 "데이터 없는데 왜 차단?"이 되기 때문.)
 */
@Singleton
class RoomBlockingPolicyProvider @Inject constructor(
    private val goalDao: GoalDao,
) : BlockingPolicyProvider {

    override suspend fun phoneGoal(): PhoneGoalPolicy? =
        goalDao.getPhoneGoal()?.let {
            PhoneGoalPolicy(goalMinutes = it.goalMinutes, limitEnabled = it.limitEnabled)
        }

    override suspend fun watchedApps(): List<AppBlockingPolicy> =
        goalDao.getAppGoals().map {
            AppBlockingPolicy(
                packageName = it.packageName,
                appLabel = it.appLabel,
                goalMinutes = it.goalMinutes,
                limitEnabled = it.limitEnabled,
            )
        }
}

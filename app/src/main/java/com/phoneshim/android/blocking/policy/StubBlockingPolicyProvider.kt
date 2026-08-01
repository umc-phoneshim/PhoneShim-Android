package com.phoneshim.android.blocking.policy

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 데이터 계약 전 임시 구현. 온보딩 연동이 붙으면 이 클래스를 실제 provider로 교체하면 된다.
 * 온보딩/설정 저장소에서 실제 값 주입.
 */
@Singleton
class StubBlockingPolicyProvider @Inject constructor() : BlockingPolicyProvider {

    override suspend fun phoneGoal(): PhoneGoalPolicy =
        PhoneGoalPolicy(goalMinutes = 210, limitEnabled = true) // 3h30m (시안 예시값)

    override suspend fun watchedApps(): List<AppBlockingPolicy> = listOf(
        AppBlockingPolicy("com.kakao.talk", "카카오톡", goalMinutes = 60, limitEnabled = true),
        AppBlockingPolicy("com.facebook.katana", "페이스북", goalMinutes = 90, limitEnabled = true),
        AppBlockingPolicy("com.zhiliaoapp.musically", "틱톡", goalMinutes = 60, limitEnabled = true),
    )
}
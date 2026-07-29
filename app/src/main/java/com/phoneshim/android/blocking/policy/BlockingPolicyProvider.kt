package com.phoneshim.android.blocking.policy

/**
 * 차단 정책의 출처. 온보딩/설정 데이터가 실제 소스지만, 계약이 잡히기 전까지
 * 엔진은 이 인터페이스에만 의존한다. 나중에 data 레이어가 구현체로 교체.
 */
interface BlockingPolicyProvider {

    /** 전체 폰 하루 목표. 설정된 값이 없으면 null. */
    suspend fun phoneGoal(): PhoneGoalPolicy?

    /** 주의앱 정책 목록. */
    suspend fun watchedApps(): List<AppBlockingPolicy>
}

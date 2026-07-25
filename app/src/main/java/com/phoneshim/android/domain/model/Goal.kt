package com.phoneshim.android.domain.model

// 사용자가 설정한 스마트폰 사용 목표 (온보딩/설정 공용 도메인 모델)
// 온보딩(setgoal)·설정(pref) 각 UI 상태가 이 모델로 매핑되어 저장/조회됩니다.
data class Goal(
    val id: String? = null,
    val gender: String? = null,
    val ageGroup: String? = null,
    val dailyGoalMinutes: Int = 0,        // 하루 총 목표 사용 시간(분)
    val blockAfterGoal: Boolean = false,  // 목표 시간 이후 폰 금지
    val apps: List<AppGoal> = emptyList(),
)

// 목표 대상 앱별 설정 (패키지명 기준)
data class AppGoal(
    val packageName: String,
    val appName: String,
    val goalMinutes: Int,        // 앱별 목표 시간(분)
    val accessLimited: Boolean,  // 접근 제한 여부
)

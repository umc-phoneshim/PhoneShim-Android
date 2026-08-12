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
    val goalMinutes: Int,        // 앱별 목표 시간(분) — 서버 targetMinutes
    val accessLimited: Boolean,  // 접근 제한 여부 — 서버 restrictAfter
    // 앱별 목표 진입 횟수 — 서버 targetCount. 서버는 필수(1 이상)로 요구하지만
    // 아직 이 값을 입력받는 화면이 없어(Figma 04-4 접근 횟수 팝업 미구현) 최소값을 기본으로 둔다.
    val targetCount: Int = 1,
    // 이 앱의 목표를 세운 이유 — 서버 goalReason. 공백 포함 최대 100자, 선택 입력.
    // 설정(PREF)의 '어플 목표 설정' 문구가 이 값이다.
    // User 의 motivation(메인 화면 다짐 문구)과는 다른 값이므로 섞어 쓰지 않는다.
    val goalReason: String? = null,
)

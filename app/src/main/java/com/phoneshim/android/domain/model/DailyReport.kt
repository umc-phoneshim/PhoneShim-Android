package com.phoneshim.android.domain.model

/**
 * 리포트 화면이 사용하는 하루치 데이터 묶음.
 *
 * 서버에 "일별 리포트" 단일 엔드포인트는 없습니다. 아래 두 소스를 합쳐 구성합니다.
 * - GET /api/usage-logs?date=  : 앱별 일별 사용량 (폴 담당, 구현완료)
 * - GET /api/usage-logs/status : 앱 이름/목표까지 포함한 오늘 현황 (구현완료)
 */
data class DailyReport(
    val date: String,
    val appUsages: List<AppUsage>,
) {
    val totalUsedMinutes: Int get() = appUsages.sumOf { it.usedMinutes }
    val totalEntryCount: Int get() = appUsages.sumOf { it.entryCount }
    val isEmpty: Boolean get() = appUsages.isEmpty()
}

/**
 * 주의 앱 하나의 하루 사용량.
 *
 * [appName] 과 [packageName] 은 /api/usage-logs/status 에서만 내려옵니다.
 * 과거 날짜 조회(/api/usage-logs)에서는 비어 있을 수 있습니다.
 */
data class AppUsage(
    val monitoredAppId: String,
    val appName: String = "",
    val packageName: String = "",
    val usedMinutes: Int,
    val entryCount: Int,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
)

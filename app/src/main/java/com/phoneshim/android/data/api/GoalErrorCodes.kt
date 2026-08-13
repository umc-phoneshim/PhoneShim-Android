package com.phoneshim.android.data.api

/**
 * 목표 도메인(MonitoredApp/TotalGoal/AppGoal) 서버 에러 코드. 명세서 2·5번 시트 기준.
 *
 * 공통 코드(UNAUTHORIZED, VALIDATION_ERROR 등)는 [ApiErrorCodes]에 있습니다.
 * 여기 따로 둔 이유는 [ApiErrorCodes]가 다른 도메인 소유자의 파일이라,
 * 목표 도메인 코드를 섞지 않고 소유 경계를 지키기 위해서입니다.
 */
object GoalErrorCodes {
    // MonitoredApp
    const val MONITORED_APP_NOT_FOUND = "MONITORED_APP_NOT_FOUND"
    const val MONITORED_APP_ALREADY_EXISTS = "MONITORED_APP_ALREADY_EXISTS"
    const val MONITORED_APP_LIMIT_EXCEEDED = "MONITORED_APP_LIMIT_EXCEEDED"

    // TotalGoal
    const val TOTAL_GOAL_NOT_FOUND = "TOTAL_GOAL_NOT_FOUND"
    const val TOTAL_GOAL_ALREADY_EXISTS = "TOTAL_GOAL_ALREADY_EXISTS"

    // AppGoal
    const val APP_GOAL_NOT_FOUND = "APP_GOAL_NOT_FOUND"
    const val APP_GOAL_ALREADY_EXISTS = "APP_GOAL_ALREADY_EXISTS"

    /**
     * Reminder 의 restrictedAppIds 에 삭제됐거나 본인 소유가 아닌 주의 앱 id 가 섞인 경우.
     * 클라이언트가 들고 있는 주의 앱 목록이 서버와 어긋났다는 신호이므로,
     * MonitoredAppRepository.refreshMonitoredApps() 로 서버 목록을 다시 받아 맞춥니다.
     */
    const val INVALID_RESTRICTED_APP_IDS = "INVALID_RESTRICTED_APP_IDS"

    // 목표 값 검증
    const val INVALID_TARGET_MINUTES = "INVALID_TARGET_MINUTES"
    const val INVALID_TARGET_COUNT = "INVALID_TARGET_COUNT"
    const val INVALID_GOAL_REASON = "INVALID_GOAL_REASON"
}

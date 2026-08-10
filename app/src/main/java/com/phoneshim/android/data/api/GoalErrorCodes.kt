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

    // 목표 값 검증
    const val INVALID_TARGET_MINUTES = "INVALID_TARGET_MINUTES"
    const val INVALID_TARGET_COUNT = "INVALID_TARGET_COUNT"
    const val INVALID_GOAL_REASON = "INVALID_GOAL_REASON"
}

/**
 * 서버가 검증하는 값 범위. 요청을 보내기 전에 클라에서 같은 기준으로 걸러
 * 불필요한 400 왕복을 줄입니다. 서버 명세가 바뀌면 여기부터 고칩니다.
 */
object GoalLimits {
    /** 주의 앱 최대 등록 개수. */
    const val MAX_MONITORED_APPS = 5

    /** 목표 사용 시간(분) 허용 범위. 10분 미만·1430분 초과는 400. */
    const val MIN_TARGET_MINUTES = 10
    const val MAX_TARGET_MINUTES = 1430

    /** 앱별 목표 진입 횟수 최소값. */
    const val MIN_TARGET_COUNT = 1

    /** 목표 이유 최대 길이(공백 포함). */
    const val MAX_GOAL_REASON_LENGTH = 100
}

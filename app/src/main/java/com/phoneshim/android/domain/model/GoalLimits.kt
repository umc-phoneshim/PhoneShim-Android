package com.phoneshim.android.domain.model

/**
 * 서버가 검증하는 목표 값의 허용 범위.
 *
 * [왜 domain 에 있나]
 *   같은 숫자를 UI 검증과 API 계약이 함께 본다. 원래 data/api 쪽에 있어서 UI 가
 *   참조하려면 화면이 data 계층을 직접 의존해야 했고, 그걸 피하느라 setgoal 뷰모델이
 *   같은 값을 따로 들고 있었다. 두 벌이 되면 서버 범위가 바뀔 때 한쪽만 고쳐진다.
 *   양쪽이 모두 의존해도 되는 곳은 domain 이라 여기로 옮긴다.
 *
 * 서버 명세가 바뀌면 여기부터 고친다.
 */
object GoalLimits {
    /** 주의 앱 최대 등록 개수. 초과하면 400 MONITORED_APP_LIMIT_EXCEEDED. */
    const val MAX_MONITORED_APPS = 5

    /**
     * 목표 사용 시간(분) 허용 범위. 벗어나면 400 INVALID_TARGET_MINUTES.
     *
     * 저장 경로가 로컬 우선이라 범위를 넘긴 값은 로컬에만 들어가고 서버 동기화만
     * 조용히 실패한다. 그래서 요청을 보내기 전에 클라에서 같은 기준으로 거른다.
     */
    const val MIN_TARGET_MINUTES = 10
    const val MAX_TARGET_MINUTES = 1430

    /** 앱별 목표 진입 횟수 최소값. */
    const val MIN_TARGET_COUNT = 1

    /** 목표 이유 최대 길이(공백 포함). */
    const val MAX_GOAL_REASON_LENGTH = 100
}
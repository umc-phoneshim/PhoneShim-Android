package com.phoneshim.android.domain.model

/**
 * "쉼이의 제안".
 *
 * 클라이언트가 AI를 호출하는 구조가 아니라, 백엔드가 사용 빈도 등을 분석해
 * 완성된 문구를 내려주면 화면은 그대로 출력만 합니다.
 * POST /api/ai/daily-feedback (예정).
 */
data class RestSuggestion(
    val date: String,
    val message: String,
)

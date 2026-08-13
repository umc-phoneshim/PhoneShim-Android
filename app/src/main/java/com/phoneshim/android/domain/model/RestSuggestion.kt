package com.phoneshim.android.domain.model

/**
 * 쉼이의 제안. GET /api/reports/suggestion?date=YYYY-MM-DD
 *
 * AI가 아니라 백엔드가 목표 대비 사용량을 보고 정해진 문구 템플릿을 고른 결과입니다.
 * 화면은 [message] 를 그대로 출력하고, [suggestionType] 으로 표현만 다르게 합니다.
 */
data class RestSuggestion(
    val suggestionType: SuggestionType,
    val message: String,
    /** 목표를 넘긴 분. 달성했거나 목표가 없으면 0. */
    val excessMinutes: Int,
    /** 문구에서 언급하는 앱 이름. 없을 수 있습니다. */
    val appName: String? = null,
) {
    val isAchieved: Boolean get() = suggestionType == SuggestionType.ACHIEVED
    val needsGoal: Boolean get() = suggestionType == SuggestionType.NO_GOAL
}

/** 백엔드 reportEntity 의 SuggestionType 과 동일합니다. */
enum class SuggestionType {
    /** 폰 전체 사용이 목표를 넘김. */
    TOTAL_EXCEEDED,

    /** 특정 주의 앱이 목표를 넘김. */
    APP_EXCEEDED,

    /** 목표 달성. */
    ACHIEVED,

    /** 전체 목표가 아직 없음. */
    NO_GOAL,

    UNKNOWN,
    ;

    companion object {
        fun from(raw: String?): SuggestionType = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

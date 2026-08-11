package com.phoneshim.android.domain.model

/**
 * 사용 이유 고정 선택지. 백엔드 prisma enum UsageReasonCode 와 동일합니다.
 *
 * 자유 입력이 아니라 5개 객관식이며, 한 시간 블록에 복수 선택할 수 있습니다.
 * (스키마 주석: "사용 이유 팝업의 고정 객관식 선택지 — Figma REP-01")
 */
enum class UsageReasonCode(val label: String) {
    LEISURE("여가 시간"),
    COMMUTE("이동 중"),
    HABIT("습관적으로"),
    INFO("정보성"),
    OTHER("기타"),
    ;

    companion object {
        fun from(raw: String?): UsageReasonCode? = entries.firstOrNull { it.name == raw }
    }
}

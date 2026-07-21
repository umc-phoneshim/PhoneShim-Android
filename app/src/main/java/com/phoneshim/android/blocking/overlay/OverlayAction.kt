package com.phoneshim.android.blocking.overlay

/**
 * 오버레이 화면에서 발생하는 사용자 행동.
 *
 * UI(BlockOverlayContent)가 내보내고, 엔진(BlockerService)이 소유·처리한다.
 *
 */
sealed interface OverlayAction {
    data object Dismiss : OverlayAction                 // 확인 / 좋아요
    data object OpenPhoneShim : OverlayAction           // 폰쉼 어플
    data object Call : OverlayAction                    // 전화
    data object Message : OverlayAction                 // 메시지
    data class ReasonSubmitted(val packageName: String, val reason: String) : OverlayAction // 완료
}

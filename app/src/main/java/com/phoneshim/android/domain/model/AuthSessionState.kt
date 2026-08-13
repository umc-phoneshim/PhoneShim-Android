package com.phoneshim.android.domain.model

/**
 * 앱 전역에서 관찰하는 인증 세션 상태입니다.
 *
 * Access Token 자체는 노출하지 않습니다. 인증이 필요한 기능은 [AUTHENTICATED] 상태를
 * 확인한 뒤 연결 또는 요청 직전에 TokenProvider에서 최신 토큰을 조회해야 합니다.
 */
enum class AuthSessionState {
    RESTORING,
    AUTHENTICATED,
    UNAUTHENTICATED,
}

package com.phoneshim.android.ui.common.base

/**
 * 화면이 이해하는 오류 형태.
 *
 * Retrofit/HTTP/직렬화 예외 타입은 이 타입 밖으로 나가지 않는다.
 * 변환은 [UiErrorMapper] 한 곳에서만 하고, ViewModel 과 화면은 이 값만 본다.
 * 네트워크 계층 구현이 바뀌어도 매퍼만 고치면 되도록 두기 위한 경계다.
 */
data class UiError(
    val kind: Kind,
    /** 사용자에게 그대로 보여줄 수 있는 문구. */
    val message: String,
    /**
     * 서버 오류 코드. 도메인 고유 분기에만 쓴다.
     *
     * 공통 계층은 "이 코드가 무슨 의미인지"를 알지 않는다.
     */
    val code: String? = null,
) {
    enum class Kind {
        /** 인증 토큰 없음/무효. 로그인 화면으로 보내야 한다. */
        AUTH,

        /** 연결 실패. 같은 요청을 그대로 다시 보내면 된다. */
        NETWORK,

        /** 서버가 코드와 함께 거절. 같은 요청을 다시 보내도 결과가 같다. */
        SERVER,

        /** 응답 파싱 실패 등 위 어디에도 안 맞는 경우. */
        UNKNOWN,
    }

    /** 같은 요청을 그대로 재시도해도 되는 오류인가. */
    val isRetryable: Boolean get() = kind == Kind.NETWORK
}

/**
 * 기능과 무관하게 처리가 같은 효과.
 *
 * [BaseViewModel] 의 기능별 effect 채널은 타입(F)이 기능마다 달라 공통 효과를 실을 수 없다.
 * F 에 인터페이스를 강제하면 모든 기능 파일을 건드리게 되므로 채널을 따로 둔다.
 */
sealed interface CommonUiEffect {
    /** 401. 화면이 어디였든 로그인으로 보낸다. */
    data object AuthExpired : CommonUiEffect
}
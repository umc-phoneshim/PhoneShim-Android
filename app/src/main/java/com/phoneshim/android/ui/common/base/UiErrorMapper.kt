package com.phoneshim.android.ui.common.base

import com.phoneshim.android.data.api.common.ApiException

/**
 * [ApiException] -> [UiError] 변환.
 *
 * 이 파일이 네트워크 예외 타입을 아는 유일한 지점이다.
 *
 * 인증 만료 판별을 HTTP 상태 코드 401 로만 하는 근거:
 * API 명세 0_공통정보의 인증 에러는 401 UNAUTHORIZED(토큰 없음)와
 * 401 INVALID_TOKEN(토큰 무효) 둘뿐이고 상태 코드가 같다.
 * 성공 응답(2xx) + success:false 로 인증 오류가 오는 경로는 명세에 없으므로
 * [ApiException.Server] 는 인증으로 보지 않는다.
 */
object UiErrorMapper {

    fun map(throwable: Throwable): UiError = when (throwable) {
        is ApiException.Http -> mapHttp(throwable)

        // 2xx + success:false. 서버가 코드와 함께 거절한 경우.
        is ApiException.Server -> UiError(
            kind = UiError.Kind.SERVER,
            message = throwable.error.message.ifBlank { UNKNOWN_MESSAGE },
            code = throwable.error.code,
        )

        // 연결 실패/타임아웃. 같은 요청을 그대로 재시도할 수 있다.
        is ApiException.Network -> UiError(
            kind = UiError.Kind.NETWORK,
            message = NETWORK_MESSAGE,
        )

        // 응답 형태가 계약과 다른 경우. 재시도해도 같으므로 사용자에게는 일반 오류로 보인다.
        is ApiException.Serialization,
        is ApiException.InvalidResponse,
        is ApiException.Unexpected,
            -> UiError(
            kind = UiError.Kind.UNKNOWN,
            message = UNKNOWN_MESSAGE,
        )

        else -> UiError(
            kind = UiError.Kind.UNKNOWN,
            message = UNKNOWN_MESSAGE,
        )
    }

    private fun mapHttp(throwable: ApiException.Http): UiError {
        if (throwable.statusCode == HTTP_UNAUTHORIZED) {
            return UiError(
                kind = UiError.Kind.AUTH,
                message = AUTH_MESSAGE,
                code = throwable.error?.code,
            )
        }

        return UiError(
            kind = UiError.Kind.SERVER,
            // 서버 문구가 있으면 그대로 쓴다. 화면이 code 로 자기 도메인 문구를 덮어쓸 수 있다.
            message = throwable.error?.message?.takeIf { it.isNotBlank() } ?: UNKNOWN_MESSAGE,
            code = throwable.error?.code,
        )
    }

    private const val HTTP_UNAUTHORIZED = 401

    private const val AUTH_MESSAGE = "로그인이 만료되었어요. 다시 로그인해 주세요."
    private const val NETWORK_MESSAGE = "네트워크에 연결할 수 없어요. 연결 상태를 확인해 주세요."
    private const val UNKNOWN_MESSAGE = "문제가 발생했어요. 잠시 후 다시 시도해 주세요."
}
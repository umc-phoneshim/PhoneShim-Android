package com.phoneshim.android.domain.model

/**
 * 회원 탈퇴 요청 결과.
 *
 * 서버는 즉시 삭제하지 않고 14일 동안 [UserStatus.WITHDRAWAL_PENDING] 상태로 보존합니다.
 * 이 기간 안에는 POST /api/auth/recover-withdrawal 로 복구할 수 있습니다.
 */
data class WithdrawalResult(
    val status: UserStatus = UserStatus.WITHDRAWAL_PENDING,
    /** 복구 가능 마감 시각(ISO 8601). 서버가 내려주지 않으면 null. */
    val recoverableUntil: String? = null,
) {
    companion object {
        /** 유예 기간(일). 명세 4_Auth_User 기준. */
        const val GRACE_PERIOD_DAYS = 14
    }
}

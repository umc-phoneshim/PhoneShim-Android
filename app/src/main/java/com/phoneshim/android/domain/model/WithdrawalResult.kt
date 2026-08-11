package com.phoneshim.android.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 회원 탈퇴 요청 결과. DELETE /api/auth/withdraw 응답입니다.
 *
 * 서버는 즉시 삭제하지 않고 14일 동안 [UserStatus.WITHDRAWAL_PENDING] 상태로 보존합니다.
 *
 * @param withdrawalRequestedAt 탈퇴 요청 시각(ISO 8601). 서버가 내려주는 값입니다.
 */
data class WithdrawalResult(
    val status: UserStatus = UserStatus.WITHDRAWAL_PENDING,
    val withdrawalRequestedAt: String? = null,
) {
    /**
     * 복구 가능 마감일(yyyy.MM.dd). 요청 시각에서 [GRACE_PERIOD_DAYS] 를 더해 계산합니다.
     * 서버가 마감일을 직접 주지 않아 클라이언트에서 계산합니다.
     */
    val recoverableUntil: String?
        get() = withdrawalRequestedAt
            ?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?.plus(java.time.Duration.ofDays(GRACE_PERIOD_DAYS.toLong()))
            ?.atZone(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

    companion object {
        /** 유예 기간(일). 백엔드 withdrawUserService 기준. */
        const val GRACE_PERIOD_DAYS = 14
    }
}

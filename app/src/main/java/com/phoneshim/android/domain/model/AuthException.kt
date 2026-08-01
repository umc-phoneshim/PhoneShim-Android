package com.phoneshim.android.domain.model

sealed class AuthException(message: String) : Exception(message) {
    data object WithdrawalPending : AuthException("탈퇴 유예 중인 계정입니다.")

    class FeatureUnavailable(
        val feature: PendingAuthFeature,
    ) : AuthException("${feature.name} 기능은 서버 준비 중입니다.")
}

enum class PendingAuthFeature {
    LOGOUT,
    RECOVER_WITHDRAWAL,
    LINK_ACCOUNT,
}

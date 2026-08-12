package com.phoneshim.android.domain.model

sealed class AuthException(message: String) : Exception(message) {
    data object WithdrawalPending : AuthException("탈퇴 유예 중인 계정입니다.")

    data object GoogleCredentialUnavailable : AuthException(
        "기기에서 사용할 수 있는 Google 계정을 찾지 못했습니다.",
    )

    class FeatureUnavailable(
        val feature: PendingAuthFeature,
    ) : AuthException("${feature.name} 기능은 서버 준비 중입니다.")
}

enum class PendingAuthFeature {
    GOOGLE_LOGIN_TOKEN_CONTRACT,
    LOGOUT,
    RECOVER_WITHDRAWAL,
    LINK_ACCOUNT,
}

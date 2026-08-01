package com.phoneshim.android.domain.model

sealed interface SocialLoginResult {
    data object NewUser : SocialLoginResult
    data object ExistingUser : SocialLoginResult
}

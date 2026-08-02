package com.phoneshim.android.domain.model

data class AuthFeatureAvailability(
    val canRecoverWithdrawal: Boolean,
    val shouldLoadRemoteProfile: Boolean,
)

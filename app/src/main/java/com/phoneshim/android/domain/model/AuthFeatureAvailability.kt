package com.phoneshim.android.domain.model

data class AuthFeatureAvailability(
    val canGoogleLogin: Boolean,
    val shouldLoadRemoteProfile: Boolean,
)

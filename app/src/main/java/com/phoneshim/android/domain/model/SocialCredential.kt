package com.phoneshim.android.domain.model

/** A short-lived social provider credential. It must never be persisted or logged. */
data class SocialCredential(
    val provider: SocialProvider,
    val providerToken: String,
)

package com.phoneshim.android.domain.model

@JvmInline
value class AuthToken(val value: String) {
    init {
        require(value.isNotBlank()) { "Auth token must not be blank." }
    }
}

package com.phoneshim.android.auth

import javax.inject.Inject
import javax.inject.Singleton

enum class FakeAuthScenario {
    EXISTING_USER,
    NEW_USER,
    CANCELLED,
    SDK_FAILURE,
    SERVER_FAILURE,
    WITHDRAWAL_PENDING,
    RECOVERY_FAILURE,
}

@Singleton
class FakeAuthScenarioStore @Inject constructor() {
    @Volatile
    var scenario: FakeAuthScenario = FakeAuthScenario.EXISTING_USER
}

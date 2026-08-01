package com.phoneshim.android.domain.model

import javax.inject.Inject
import javax.inject.Singleton

enum class MockAuthScenario {
    EXISTING_USER,
    NEW_USER,
    CANCELLED,
    SDK_FAILURE,
    SERVER_FAILURE,
    WITHDRAWAL_PENDING,
    RECOVERY_FAILURE,
}

@Singleton
class MockAuthScenarioStore @Inject constructor() {
    @Volatile
    var scenario: MockAuthScenario = MockAuthScenario.EXISTING_USER
}

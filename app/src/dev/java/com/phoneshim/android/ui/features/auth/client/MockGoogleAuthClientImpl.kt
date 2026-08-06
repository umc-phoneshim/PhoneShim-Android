package com.phoneshim.android.ui.features.auth.client

import com.phoneshim.android.domain.model.MockAuthScenario
import com.phoneshim.android.domain.model.MockAuthScenarioStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockGoogleAuthClientImpl @Inject constructor(
    private val scenarioStore: MockAuthScenarioStore,
) : GoogleAuthClient {
    override suspend fun authenticate(): AuthClientResult = mockResult("google")

    private fun mockResult(provider: String): AuthClientResult = when (scenarioStore.scenario) {
        MockAuthScenario.CANCELLED -> AuthClientResult.Cancelled
        MockAuthScenario.SDK_FAILURE -> AuthClientResult.Failure(
            IllegalStateException("dev mock 소셜 인증 실패"),
        )
        else -> AuthClientResult.Success(
            providerToken = "dev-mock-$provider-id-token",
            providerUserId = "dev-mock-$provider-user",
            email = "mock@phoneshim.local",
        )
    }
}

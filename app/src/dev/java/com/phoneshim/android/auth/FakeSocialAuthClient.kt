package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSocialAuthClient @Inject constructor(
    private val scenarioStore: FakeAuthScenarioStore,
) : SocialAuthClient {
    override suspend fun authenticate(provider: SocialProvider): SocialAuthResult =
        when (scenarioStore.scenario) {
        FakeAuthScenario.CANCELLED -> SocialAuthResult.Cancelled
        FakeAuthScenario.SDK_FAILURE -> SocialAuthResult.Failure(
            IllegalStateException("dev 소셜 인증 실패"),
        )
        else -> SocialAuthResult.Success(
            providerAccessToken = "dev-${provider.name.lowercase()}-access-token",
            providerUserId = "dev-${provider.name.lowercase()}-user",
            email = "dev@phoneshim.local",
        )
        }
}

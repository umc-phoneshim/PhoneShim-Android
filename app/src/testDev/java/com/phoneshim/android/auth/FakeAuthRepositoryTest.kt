package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {
    @Test
    fun `existing user scenario saves session and returns existing user`() = runTest {
        val sessionStore = InMemoryAuthSessionStore()
        val scenarioStore = FakeAuthScenarioStore()
        val repository = FakeAuthRepository(sessionStore, scenarioStore)

        val result = repository.socialLogin(SocialProvider.GOOGLE, "provider-token")

        assertEquals(SocialLoginResult.ExistingUser, result.getOrThrow())
        assertTrue(sessionStore.hasSession())
    }

    @Test
    fun `new user scenario returns new user`() = runTest {
        val scenarioStore = FakeAuthScenarioStore().apply {
            scenario = FakeAuthScenario.NEW_USER
        }
        val repository = FakeAuthRepository(InMemoryAuthSessionStore(), scenarioStore)

        val result = repository.socialLogin(SocialProvider.KAKAO, "provider-token")

        assertEquals(SocialLoginResult.NewUser, result.getOrThrow())
    }

    @Test
    fun `server failure scenario does not save session`() = runTest {
        val sessionStore = InMemoryAuthSessionStore()
        val scenarioStore = FakeAuthScenarioStore().apply {
            scenario = FakeAuthScenario.SERVER_FAILURE
        }
        val repository = FakeAuthRepository(sessionStore, scenarioStore)

        val result = repository.socialLogin(SocialProvider.GOOGLE, "provider-token")

        assertTrue(result.isFailure)
        assertFalse(sessionStore.hasSession())
    }

    private class InMemoryAuthSessionStore : AuthSessionStore {
        private var token: String? = null

        override suspend fun restore(): Boolean = token != null

        override suspend fun saveAccessToken(accessToken: String) {
            token = accessToken
        }

        override suspend fun clear() {
            token = null
        }

        override fun hasSession(): Boolean = token != null
    }
}

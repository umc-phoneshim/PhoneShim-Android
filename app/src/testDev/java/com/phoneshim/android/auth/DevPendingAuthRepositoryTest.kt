package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DevPendingAuthRepositoryTest {
    private val identity = SocialIdentity(SocialProvider.KAKAO, "provider-user", "user@example.com")

    @Test
    fun `recovery stores new session in dev`() = runTest {
        val store = InMemorySessionStore()
        val repository = DevPendingAuthRepository(store, FakeAuthScenarioStore())

        val result = repository.recoverWithdrawal(identity)

        assertEquals(SocialLoginResult.ExistingUser, result.getOrThrow())
        assertTrue(store.hasSession())
    }

    @Test
    fun `logout clears local session in dev`() = runTest {
        val store = InMemorySessionStore().apply { saveAccessToken("jwt") }
        val repository = DevPendingAuthRepository(store, FakeAuthScenarioStore())

        repository.logout().getOrThrow()

        assertFalse(store.hasSession())
    }

    private class InMemorySessionStore : AuthSessionStore {
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

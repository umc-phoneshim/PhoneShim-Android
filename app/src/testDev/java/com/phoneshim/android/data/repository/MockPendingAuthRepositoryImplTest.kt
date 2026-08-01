package com.phoneshim.android.data.repository

import com.phoneshim.android.data.local.createTestTokenDataSource
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.MockAuthScenarioStore
import com.phoneshim.android.domain.model.SocialIdentity
import com.phoneshim.android.domain.model.SocialProvider
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MockPendingAuthRepositoryImplTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val identity = SocialIdentity(SocialProvider.KAKAO, "provider-user", "user@example.com")

    @Test
    fun `recovery stores new session in dev`() = runTest {
        val tokens = createTestTokenDataSource(file("recovery"))
        val result = MockPendingAuthRepositoryImpl(tokens, MockAuthScenarioStore())
            .recoverWithdrawal(identity)

        assertEquals(AuthUser(isNewUser = false), result.getOrThrow())
        assertTrue(tokens.hasToken())
    }

    @Test
    fun `logout clears local session in dev`() = runTest {
        val tokens = createTestTokenDataSource(file("logout"))
        tokens.save(AuthToken("jwt"))

        MockPendingAuthRepositoryImpl(tokens, MockAuthScenarioStore()).logout().getOrThrow()

        assertFalse(tokens.hasToken())
    }

    private fun file(name: String) = File(temporaryFolder.root, "$name.preferences_pb")
}

package com.phoneshim.android.data.repository

import com.phoneshim.android.data.local.createTestTokenDataSource
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.MockAuthScenario
import com.phoneshim.android.domain.model.MockAuthScenarioStore
import com.phoneshim.android.domain.model.SocialProvider
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MockAuthRepositoryImplTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test
    fun `existing user scenario saves session and returns existing user`() = runTest {
        val tokens = createTestTokenDataSource(file("existing"))
        val repository = MockAuthRepositoryImpl(tokens, MockAuthScenarioStore())

        val result = repository.socialLogin(SocialProvider.GOOGLE, "provider-token")

        assertEquals(SocialLoginResult(isNewUser = false), result.getOrThrow())
        assertTrue(tokens.hasSession())
    }

    @Test
    fun `new user scenario returns new user`() = runTest {
        val scenario = MockAuthScenarioStore().apply { this.scenario = MockAuthScenario.NEW_USER }
        val repository = MockAuthRepositoryImpl(createTestTokenDataSource(file("new")), scenario)

        assertEquals(
            SocialLoginResult(isNewUser = true),
            repository.socialLogin(SocialProvider.KAKAO, "provider-token").getOrThrow(),
        )
    }

    @Test
    fun `server failure scenario does not save session`() = runTest {
        val tokens = createTestTokenDataSource(file("failure"))
        val scenario = MockAuthScenarioStore().apply { this.scenario = MockAuthScenario.SERVER_FAILURE }
        val result = MockAuthRepositoryImpl(tokens, scenario)
            .socialLogin(SocialProvider.GOOGLE, "provider-token")

        assertTrue(result.isFailure)
        assertFalse(tokens.hasSession())
    }

    private fun file(name: String) = File(temporaryFolder.root, "$name.preferences_pb")
}

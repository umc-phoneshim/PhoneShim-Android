package com.phoneshim.android.auth

import com.google.gson.Gson
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.SocialLoginResult
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.domain.repository.AuthSessionStore
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteAuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var sessionStore: InMemoryAuthSessionStore
    private lateinit var repository: RemoteAuthRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val gson = Gson()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
        sessionStore = InMemoryAuthSessionStore()
        repository = RemoteAuthRepository(api, ApiCallExecutor(gson), sessionStore)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `google 200 response stores jwt and returns existing user`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(successBody(isNewUser = false)),
        )

        val result = repository.socialLogin(SocialProvider.GOOGLE, "google-token")

        assertEquals(SocialLoginResult.ExistingUser, result.getOrThrow())
        assertEquals("phoneshim-jwt", sessionStore.token)
        val request = server.takeRequest()
        assertEquals("/api/auth/google", request.path)
        assertTrue(request.body.readUtf8().contains("google-token"))
    }

    @Test
    fun `kakao 201 response stores jwt and returns new user`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(successBody(isNewUser = true)),
        )

        val result = repository.socialLogin(SocialProvider.KAKAO, "kakao-token")

        assertEquals(SocialLoginResult.NewUser, result.getOrThrow())
        assertEquals("/api/auth/kakao", server.takeRequest().path)
    }

    @Test
    fun `409 response preserves server error and does not save jwt`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"success":false,"error":{"code":"ACCOUNT_WITHDRAWAL_PENDING","message":"복구가 필요합니다."}}""",
                ),
        )

        val result = repository.socialLogin(SocialProvider.GOOGLE, "google-token")

        assertEquals(AuthException.WithdrawalPending, result.exceptionOrNull())
        assertFalse(sessionStore.hasSession())
    }

    private fun successBody(isNewUser: Boolean): String =
        """{"success":true,"data":{"accessToken":"phoneshim-jwt","isNewUser":$isNewUser}}"""

    private class InMemoryAuthSessionStore : AuthSessionStore {
        var token: String? = null

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

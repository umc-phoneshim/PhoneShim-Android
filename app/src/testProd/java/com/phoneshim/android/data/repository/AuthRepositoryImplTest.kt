package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.data.local.createTestTokenDataSource
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.AuthUser
import com.phoneshim.android.domain.model.SocialProvider
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryImplTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private lateinit var server: MockWebServer
    private lateinit var api: AuthApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }

    @After fun tearDown() = server.shutdown()

    @Test
    fun `google 200 response stores jwt and returns existing user`() = runTest {
        server.enqueue(successResponse(200, isNewUser = false))
        val tokens = tokens("google")

        val result = repository(tokens).socialLogin(SocialProvider.GOOGLE, "google-token")

        assertEquals(AuthUser(isNewUser = false), result.getOrThrow())
        assertEquals("phoneshim-jwt", tokens.getAccessToken())
        assertEquals("/api/auth/google", server.takeRequest().path)
    }

    @Test
    fun `kakao 201 response stores jwt and returns new user`() = runTest {
        server.enqueue(successResponse(201, isNewUser = true))

        val result = repository(tokens("kakao"))
            .socialLogin(SocialProvider.KAKAO, "kakao-token")

        assertEquals(AuthUser(isNewUser = true), result.getOrThrow())
        assertEquals("/api/auth/kakao", server.takeRequest().path)
    }

    @Test
    fun `409 response maps withdrawal pending and does not save jwt`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(409).setHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"error":{"code":"ACCOUNT_WITHDRAWAL_PENDING","message":"복구가 필요합니다."}}"""),
        )
        val tokens = tokens("pending")

        val result = repository(tokens).socialLogin(SocialProvider.GOOGLE, "google-token")

        assertEquals(AuthException.WithdrawalPending, result.exceptionOrNull())
        assertFalse(tokens.hasToken())
    }

    private fun TestScope.tokens(name: String) = createTestTokenDataSource(
        File(temporaryFolder.root, "$name.preferences_pb"),
    )

    private fun repository(tokens: TokenDataSource) =
        AuthRepositoryImpl(api, ApiCallExecutor(gson), tokens)

    private fun successResponse(code: Int, isNewUser: Boolean) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":{"accessToken":"phoneshim-jwt","isNewUser":$isNewUser}}""")
}

package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.data.local.createTestTokenDataSource
import com.phoneshim.android.domain.model.SocialCredential
import com.phoneshim.android.domain.model.SocialProvider
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PendingAuthRepositoryImplTest {
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
    fun `recovery sends google id token and stores returned jwt`() = runTest {
        server.enqueue(successResponse("""{"accessToken":"recovered-jwt","user":{}}"""))
        val tokens = tokens("recovery")

        repository(tokens).recoverWithdrawal(
            SocialCredential(SocialProvider.GOOGLE, "google-id-token"),
        ).getOrThrow()

        assertEquals("recovered-jwt", tokens.getAccessToken())
        val request = server.takeRequest()
        assertEquals("/api/auth/recover-withdrawal", request.path)
        assertEquals(
            """{"provider":"GOOGLE","idToken":"google-id-token"}""",
            request.body.readUtf8(),
        )
    }

    @Test
    fun `link account sends kakao access token`() = runTest {
        server.enqueue(
            successResponse(
                """{"id":"link-id","provider":"KAKAO","providerUserId":"user-id"}""",
            ),
        )

        repository(tokens("link")).linkAccount(
            SocialCredential(SocialProvider.KAKAO, "kakao-access-token"),
        ).getOrThrow()

        val request = server.takeRequest()
        assertEquals("/api/auth/link-account", request.path)
        assertEquals(
            """{"provider":"KAKAO","accessToken":"kakao-access-token"}""",
            request.body.readUtf8(),
        )
    }

    @Test
    fun `logout accepts no content response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        repository(tokens("logout")).logout().getOrThrow()

        assertEquals("/api/auth/logout", server.takeRequest().path)
    }

    private fun TestScope.tokens(name: String) = createTestTokenDataSource(
        File(temporaryFolder.root, "$name.preferences_pb"),
    )

    private fun repository(tokens: TokenDataSource) = PendingAuthRepositoryImpl(
        authApi = api,
        apiCallExecutor = ApiCallExecutor(gson),
        tokenDataSource = tokens,
    )

    private fun successResponse(data: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"data":$data}""")
}

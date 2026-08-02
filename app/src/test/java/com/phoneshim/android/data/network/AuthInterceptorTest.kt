package com.phoneshim.android.data.network

import com.phoneshim.android.data.local.TokenProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `token is attached to authenticated request`() {
        execute(token = "phoneshim-jwt")

        assertEquals("Bearer phoneshim-jwt", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `authorization is omitted when token is missing`() {
        execute(token = null)

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `no auth control header skips token and is removed before network`() {
        execute(token = "phoneshim-jwt", noAuth = true)

        val request = server.takeRequest()
        assertNull(request.getHeader("Authorization"))
        assertNull(request.getHeader(AuthInterceptor.NO_AUTH_HEADER))
    }

    private fun execute(token: String?, noAuth: Boolean = false) {
        server.enqueue(MockResponse().setResponseCode(200))
        val provider = object : TokenProvider {
            override fun getAccessToken(): String? = token
        }
        val request = Request.Builder()
            .url(server.url("/api/users/me"))
            .apply {
                if (noAuth) header(AuthInterceptor.NO_AUTH_HEADER, "true")
            }
            .build()
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(provider))
            .build()
            .newCall(request)
            .execute()
            .close()
    }
}

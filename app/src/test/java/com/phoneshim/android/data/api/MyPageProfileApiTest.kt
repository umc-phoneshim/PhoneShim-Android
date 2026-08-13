package com.phoneshim.android.data.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyPageProfileApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: MyPageApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(MyPageApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `온보딩 endpoint로 성별과 연령대를 수정한다`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody(
                    """{"success":true,"data":{"email":"user@test.com","name":"타로","gender":"FEMALE","ageGroup":"FIFTIES_PLUS"}}""",
                ),
        )

        val response = api.updateUserProfile(
            UpdateUserProfileRequest(gender = "FEMALE", ageGroup = "FIFTIES_PLUS"),
        )
        val request = server.takeRequest()
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject

        assertEquals("PATCH", request.method)
        assertEquals("/api/users/me/onboarding", request.path)
        assertEquals("FEMALE", body["gender"].asString)
        assertEquals("FIFTIES_PLUS", body["ageGroup"].asString)
        assertEquals("FIFTIES_PLUS", response.data?.ageGroup)
    }
}

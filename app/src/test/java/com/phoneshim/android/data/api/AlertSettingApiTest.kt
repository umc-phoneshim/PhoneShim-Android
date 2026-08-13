package com.phoneshim.android.data.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AlertSettingApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: AlertSettingApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(AlertSettingApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `알림 설정을 조회한다`() = runTest {
        server.enqueue(successResponse())

        val response = api.getAlertSetting()
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/api/alert-settings", request.path)
        assertTrue(response.success)
        assertEquals(1320, response.data?.alertTimeMinutes)
        assertEquals(true, response.data?.enabled)
    }

    @Test
    fun `알림 시간을 분 단위로 수정한다`() = runTest {
        server.enqueue(successResponse(minutes = 1380))

        val response = api.updateAlertSetting(UpdateAlertSettingRequest(1380))
        val request = server.takeRequest()
        val json = JsonParser.parseString(request.body.readUtf8()).asJsonObject

        assertEquals("PATCH", request.method)
        assertEquals("/api/alert-settings", request.path)
        assertEquals(1380, json["alertTimeMinutes"].asInt)
        assertEquals(true, response.data?.enabled)
    }

    private fun successResponse(minutes: Int = 1320): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(
            """{"success":true,"data":${settingJson(minutes)},"error":null}""",
        )

    private fun settingJson(minutes: Int) = """
        {
          "id":"alert-1",
          "userId":"user-1",
          "enabled":true,
          "alertTimeMinutes":$minutes,
          "createdAt":"2026-08-13T00:00:00.000Z",
          "updatedAt":"2026-08-13T01:00:00.000Z"
        }
    """.trimIndent()
}

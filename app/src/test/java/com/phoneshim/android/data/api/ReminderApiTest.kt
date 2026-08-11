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

class ReminderApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ReminderApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(ReminderApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `선택한 날짜로 리마인더 목록을 조회한다`() = runTest {
        server.enqueue(successResponse("[$reminderJson]"))

        val response = api.getReminders("2026-07-16")
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/api/reminders?date=2026-07-16", request.path)
        assertTrue(response.success)
        assertEquals("reminder-1", response.data?.single()?.id)
    }

    @Test
    fun `리마인더를 생성한다`() = runTest {
        server.enqueue(successResponse(reminderJson))
        val body = CreateReminderRequest(
            date = "2026-07-16",
            title = "과제하기",
            startTime = "2026-07-16T01:00:00Z",
            endTime = "2026-07-16T02:00:00Z",
            restrictMode = "SPECIFIC_APP",
            restrictedAppIds = listOf("app-1"),
        )

        api.createReminder(body)
        val request = server.takeRequest()
        val json = JsonParser.parseString(request.body.readUtf8()).asJsonObject

        assertEquals("POST", request.method)
        assertEquals("/api/reminders", request.path)
        assertEquals("2026-07-16", json["date"].asString)
        assertEquals("SPECIFIC_APP", json["restrictMode"].asString)
        assertEquals("app-1", json["restrictedAppIds"].asJsonArray[0].asString)
    }

    @Test
    fun `리마인더 일부 필드를 수정한다`() = runTest {
        server.enqueue(successResponse(reminderJson))

        api.updateReminder("reminder-1", UpdateReminderRequest(title = "수정된 할 일"))
        val request = server.takeRequest()
        val json = JsonParser.parseString(request.body.readUtf8()).asJsonObject

        assertEquals("PATCH", request.method)
        assertEquals("/api/reminders/reminder-1", request.path)
        assertEquals("수정된 할 일", json["title"].asString)
    }

    @Test
    fun `리마인더 상세를 조회한다`() = runTest {
        server.enqueue(successResponse(reminderJson))

        api.getReminder("reminder-1")
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/api/reminders/reminder-1", request.path)
    }

    @Test
    fun `리마인더 삭제의 204 응답을 성공으로 처리한다`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val response = api.deleteReminder("reminder-1")
        val request = server.takeRequest()

        assertEquals("DELETE", request.method)
        assertEquals("/api/reminders/reminder-1", request.path)
        assertTrue(response.isSuccessful)
    }

    private fun successResponse(data: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"success":true,"data":$data,"error":null}""")

    private companion object {
        val reminderJson = """
            {
              "id":"reminder-1",
              "userId":"user-1",
              "date":"2026-07-16T00:00:00.000Z",
              "title":"과제하기",
              "startTime":"2026-07-16T01:00:00.000Z",
              "endTime":"2026-07-16T02:00:00.000Z",
              "restrictMode":"SPECIFIC_APP",
              "restrictedAppIds":["app-1"],
              "createdAt":"2026-07-15T12:00:00.000Z",
              "updatedAt":"2026-07-15T12:00:00.000Z"
            }
        """.trimIndent()
    }
}

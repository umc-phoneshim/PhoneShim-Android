package com.phoneshim.android.data.api.common

import com.google.gson.Gson
import com.phoneshim.android.data.api.HealthApi
import com.phoneshim.android.data.api.HealthResponse
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCallExecutorTest {
    private lateinit var server: MockWebServer
    private lateinit var healthApi: HealthApi
    private val gson = Gson()
    private val executor = ApiCallExecutor(gson)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        healthApi = createHealthApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `health success returns required data`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"data":{"status":"ok"}}"""),
        )

        val result = executor.execute { healthApi.getHealth() }

        assertEquals(HealthResponse(status = "ok"), result)
        assertEquals("/health", server.takeRequest().path)
    }

    @Test
    fun `business error preserves server error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"success":false,"error":{"code":"SERVICE_UNAVAILABLE","message":"Try again later"}}""",
                ),
        )

        val error = captureException<ApiException.Server> {
            executor.execute<HealthResponse> { healthApi.getHealth() }
        }

        assertEquals("SERVICE_UNAVAILABLE", error.error.code)
        assertEquals("Try again later", error.message)
    }

    @Test
    fun `http error preserves status and parsed server error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(
                    """{"success":false,"error":{"code":"NOT_FOUND","message":"Requested resource was not found"}}""",
                ),
        )

        val error = captureException<ApiException.Http> {
            executor.execute<HealthResponse> { healthApi.getHealth() }
        }

        assertEquals(404, error.statusCode)
        assertEquals("NOT_FOUND", error.error?.code)
        assertEquals("Requested resource was not found", error.message)
    }

    @Test
    fun `unparseable http error falls back to status`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("not-json"),
        )

        val error = captureException<ApiException.Http> {
            executor.execute<HealthResponse> { healthApi.getHealth() }
        }

        assertEquals(500, error.statusCode)
        assertNull(error.error)
        assertEquals("HTTP request failed with status 500.", error.message)
    }

    @Test
    fun `malformed success response maps to serialization error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"data":"""),
        )

        captureException<ApiException.Serialization> {
            executor.execute<HealthResponse> { healthApi.getHealth() }
        }
    }

    @Test
    fun `successful response without data is invalid`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"data":null}"""),
        )

        val error = captureException<ApiException.InvalidResponse> {
            executor.execute<HealthResponse> { healthApi.getHealth() }
        }

        assertEquals("Successful response did not contain data.", error.message)
    }

    @Test
    fun `connection failure maps to network error`() = runTest {
        val unavailableServer = MockWebServer()
        unavailableServer.start()
        val unavailableApi = createHealthApi(unavailableServer)
        unavailableServer.shutdown()

        val error = captureException<ApiException.Network> {
            executor.execute<HealthResponse> { unavailableApi.getHealth() }
        }

        assertTrue(error.cause is IOException)
    }

    @Test
    fun `coroutine cancellation is preserved`() = runTest {
        val cancellation = CancellationException("cancelled")

        val error = captureException<CancellationException> {
            executor.execute<HealthResponse> { throw cancellation }
        }

        assertEquals(cancellation, error)
    }

    private fun createHealthApi(mockWebServer: MockWebServer): HealthApi =
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(HealthApi::class.java)

    private suspend inline fun <reified T : Throwable> captureException(
        crossinline block: suspend () -> Unit,
    ): T {
        try {
            block()
        } catch (error: Throwable) {
            if (error is T) return error
            throw AssertionError("Expected ${T::class.java.simpleName}, but was ${error::class.java.simpleName}", error)
        }
        throw AssertionError("Expected ${T::class.java.simpleName}, but no exception was thrown")
    }
}

package com.phoneshim.android.data.api

import com.google.gson.Gson
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiErrorCodes
import com.phoneshim.android.data.api.common.ApiException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 목표 도메인 API 3종 계약 테스트. 명세서 2_System_MonitoredApp, 5_TotalGoal_AppGoal 기준.
 *
 * 경로·메서드·쿼리·요청 body가 명세와 일치하는지, 공통 API 실행기와 대표 오류 코드가
 * ApiException으로 연결되는지를 확인합니다.
 */
class GoalApiTest {

    private lateinit var server: MockWebServer
    private lateinit var monitoredAppApi: MonitoredAppApi
    private lateinit var totalGoalApi: TotalGoalApi
    private lateinit var appGoalApi: AppGoalApi
    private val gson = Gson()
    private val apiCallExecutor = ApiCallExecutor(gson)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        monitoredAppApi = retrofit.create(MonitoredAppApi::class.java)
        totalGoalApi = retrofit.create(TotalGoalApi::class.java)
        appGoalApi = retrofit.create(AppGoalApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private fun enqueueError(code: Int, errorCode: String, message: String = "error") {
        enqueue(
            code,
            """{"success":false,"error":{"code":"$errorCode","message":"$message"}}""",
        )
    }

    private fun RecordedRequest.bodyText(): String = body.readUtf8()

    // ── MonitoredApp ────────────────────────────────────────────────

    @Test
    fun `주의 앱 등록은 POST api monitored-apps로 나가고 응답을 벗겨 돌려준다`() = runTest {
        enqueue(
            201,
            """
            {"success":true,"data":{
              "id":"app-1","userId":"user-1",
              "packageName":"com.google.android.youtube","appName":"YouTube",
              "appIcon":null,"sortOrder":0,
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"}}
            """.trimIndent(),
        )

        val result = apiCallExecutor.execute {
            monitoredAppApi.createMonitoredApp(
                MonitoredAppCreateRequest(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    sortOrder = 0,
                ),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/monitored-apps", request.path)
        assertTrue(request.bodyText().contains("\"packageName\":\"com.google.android.youtube\""))
        assertEquals("app-1", result.id)
        assertEquals(0, result.sortOrder)
        assertNull(result.appIcon)
    }

    @Test
    fun `주의 앱 목록은 배열을 그대로 돌려준다`() = runTest {
        enqueue(
            200,
            """
            {"success":true,"data":[
              {"id":"app-1","userId":"u","packageName":"com.kakao.talk","appName":"카카오톡",
               "appIcon":null,"sortOrder":0,
               "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"},
              {"id":"app-2","userId":"u","packageName":"com.google.android.youtube","appName":"YouTube",
               "appIcon":null,"sortOrder":1,
               "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"}]}
            """.trimIndent(),
        )

        val result = apiCallExecutor.execute { monitoredAppApi.getMonitoredApps() }

        assertEquals("/api/monitored-apps", server.takeRequest().path)
        assertEquals(2, result.size)
        assertEquals("com.kakao.talk", result[0].packageName)
    }

    @Test
    fun `주의 앱 수정은 PATCH로 나가고 넘긴 필드만 body에 담긴다`() = runTest {
        enqueue(
            200,
            """
            {"success":true,"data":{
              "id":"app-1","userId":"u","packageName":"com.google.android.youtube","appName":"YouTube",
              "appIcon":null,"sortOrder":1,
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T01:00:00.000Z"}}
            """.trimIndent(),
        )

        monitoredAppApi.updateMonitoredApp("app-1", MonitoredAppUpdateRequest(sortOrder = 1))

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/monitored-apps/app-1", request.path)
        val body = request.bodyText()
        assertTrue(body.contains("\"sortOrder\":1"))
        // null 필드는 요청에서 빠져야 합니다.
        assertTrue(body, !body.contains("packageName"))
    }

    @Test
    fun `주의 앱 삭제는 204 빈 본문을 정상 처리한다`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        monitoredAppApi.deleteMonitoredApp("app-1")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/monitored-apps/app-1", request.path)
    }

    @Test
    fun `주의 앱 5개 초과는 MONITORED_APP_LIMIT_EXCEEDED로 올라온다`() = runTest {
        enqueueError(400, GoalErrorCodes.MONITORED_APP_LIMIT_EXCEEDED)

        val result = apiCallExecutor.executeAsResult {
            monitoredAppApi.createMonitoredApp(
                MonitoredAppCreateRequest(packageName = "com.a", appName = "A"),
            )
        }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(GoalErrorCodes.MONITORED_APP_LIMIT_EXCEEDED, error.code)
        assertEquals(400, error.httpStatus)
    }

    @Test
    fun `없는 주의 앱 조회는 404 MONITORED_APP_NOT_FOUND로 올라온다`() = runTest {
        enqueueError(404, GoalErrorCodes.MONITORED_APP_NOT_FOUND)

        val result = apiCallExecutor.executeAsResult { monitoredAppApi.getMonitoredApp("nope") }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(GoalErrorCodes.MONITORED_APP_NOT_FOUND, error.code)
    }

    // ── TotalGoal ───────────────────────────────────────────────────

    @Test
    fun `전체 목표 생성은 POST api total-goals로 나간다`() = runTest {
        enqueue(
            201,
            """
            {"success":true,"data":{
              "id":"goal-1","userId":"u","targetMinutes":120,"restrictAfter":false,
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"}}
            """.trimIndent(),
        )

        val result = apiCallExecutor.execute {
            totalGoalApi.createTotalGoal(TotalGoalCreateRequest(targetMinutes = 120))
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/total-goals", request.path)
        assertEquals(120, result.targetMinutes)
        assertEquals(false, result.restrictAfter)
    }

    @Test
    fun `전체 목표 수정은 경로에 id가 붙지 않는다`() = runTest {
        enqueue(
            200,
            """
            {"success":true,"data":{
              "id":"goal-1","userId":"u","targetMinutes":90,"restrictAfter":true,
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T01:00:00.000Z"}}
            """.trimIndent(),
        )

        totalGoalApi.updateTotalGoal(TotalGoalUpdateRequest(targetMinutes = 90, restrictAfter = true))

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/total-goals", request.path)
    }

    @Test
    fun `목표 시간 범위를 벗어나면 INVALID_TARGET_MINUTES로 올라온다`() = runTest {
        enqueueError(400, GoalErrorCodes.INVALID_TARGET_MINUTES)

        val result = apiCallExecutor.executeAsResult {
            totalGoalApi.createTotalGoal(TotalGoalCreateRequest(targetMinutes = 5))
        }

        assertEquals(
            GoalErrorCodes.INVALID_TARGET_MINUTES,
            (result.exceptionOrNull() as ApiException).code,
        )
    }

    @Test
    fun `전체 목표가 없으면 404 TOTAL_GOAL_NOT_FOUND로 올라온다`() = runTest {
        enqueueError(404, GoalErrorCodes.TOTAL_GOAL_NOT_FOUND)

        val result = apiCallExecutor.executeAsResult { totalGoalApi.getTotalGoal() }

        assertEquals(
            GoalErrorCodes.TOTAL_GOAL_NOT_FOUND,
            (result.exceptionOrNull() as ApiException).code,
        )
    }

    // ── AppGoal ─────────────────────────────────────────────────────

    @Test
    fun `앱별 목표 조회는 monitoredAppId 쿼리로 단건을 가져온다`() = runTest {
        enqueue(
            200,
            """
            {"success":true,"data":{
              "id":"ag-1","monitoredAppId":"app-1","targetMinutes":60,"targetCount":5,
              "restrictAfter":true,"goalReason":"쇼츠를 줄이기 위해",
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"}}
            """.trimIndent(),
        )

        val result = apiCallExecutor.execute { appGoalApi.getAppGoal("app-1") }

        assertEquals("/api/app-goals?monitoredAppId=app-1", server.takeRequest().path)
        assertEquals("ag-1", result.id)
        assertEquals(5, result.targetCount)
        assertEquals("쇼츠를 줄이기 위해", result.goalReason)
    }

    @Test
    fun `앱별 목표 생성은 monitoredAppId와 목표값을 함께 보낸다`() = runTest {
        enqueue(
            201,
            """
            {"success":true,"data":{
              "id":"ag-1","monitoredAppId":"app-1","targetMinutes":60,"targetCount":5,
              "restrictAfter":true,"goalReason":null,
              "createdAt":"2026-07-07T00:00:00.000Z","updatedAt":"2026-07-07T00:00:00.000Z"}}
            """.trimIndent(),
        )

        appGoalApi.createAppGoal(
            AppGoalCreateRequest(
                monitoredAppId = "app-1",
                targetMinutes = 60,
                targetCount = 5,
                restrictAfter = true,
            ),
        )

        val request = server.takeRequest()
        assertEquals("/api/app-goals", request.path)
        val body = request.bodyText()
        assertTrue(body.contains("\"monitoredAppId\":\"app-1\""))
        assertTrue(body.contains("\"targetCount\":5"))
    }

    @Test
    fun `앱별 목표 삭제는 경로에 id를 붙인다`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        appGoalApi.deleteAppGoal("ag-1")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/app-goals/ag-1", request.path)
    }

    @Test
    fun `이미 있는 앱 목표는 409 APP_GOAL_ALREADY_EXISTS로 올라온다`() = runTest {
        enqueueError(409, GoalErrorCodes.APP_GOAL_ALREADY_EXISTS)

        val result = apiCallExecutor.executeAsResult {
            appGoalApi.createAppGoal(
                AppGoalCreateRequest(monitoredAppId = "app-1", targetMinutes = 60, targetCount = 5),
            )
        }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(GoalErrorCodes.APP_GOAL_ALREADY_EXISTS, error.code)
        assertEquals(409, error.httpStatus)
    }

    @Test
    fun `인증 만료는 401 UNAUTHORIZED로 올라오고 isUnauthorized가 참이다`() = runTest {
        enqueueError(401, ApiErrorCodes.UNAUTHORIZED)

        val result = apiCallExecutor.executeAsResult { totalGoalApi.getTotalGoal() }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(ApiErrorCodes.UNAUTHORIZED, error.code)
        assertTrue(error.isUnauthorized)
    }
}

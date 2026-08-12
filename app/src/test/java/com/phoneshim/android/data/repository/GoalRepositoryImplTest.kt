package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.AppGoalApi
import com.phoneshim.android.data.api.AppGoalCreateRequest
import com.phoneshim.android.data.api.AppGoalResponse
import com.phoneshim.android.data.api.AppGoalUpdateRequest
import com.phoneshim.android.data.api.GoalErrorCodes
import com.phoneshim.android.data.api.MonitoredAppApi
import com.phoneshim.android.data.api.MonitoredAppCreateRequest
import com.phoneshim.android.data.api.MonitoredAppResponse
import com.phoneshim.android.data.api.MonitoredAppUpdateRequest
import com.phoneshim.android.data.api.TotalGoalApi
import com.phoneshim.android.data.api.TotalGoalCreateRequest
import com.phoneshim.android.data.api.TotalGoalResponse
import com.phoneshim.android.data.api.TotalGoalUpdateRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiError
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity
import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * 목표 저장소 이관 검증.
 * 서버 3개 도메인(MonitoredApp/TotalGoal/AppGoal)과 로컬 캐시 사이의
 * packageName ↔ monitoredAppId 매핑, 폴백, 동기화 순서를 확인한다.
 */
class GoalRepositoryImplTest {

    private val goalDao = FakeGoalDao()
    private val userProfileDao = FakeUserProfileDao()
    private val monitoredAppApi = FakeMonitoredAppApi()
    private val totalGoalApi = FakeTotalGoalApi()
    private val appGoalApi = FakeAppGoalApi()
    private val apiCallExecutor = ApiCallExecutor(Gson())

    private val repository = GoalRepositoryImpl(
        monitoredAppApi = monitoredAppApi,
        totalGoalApi = totalGoalApi,
        appGoalApi = appGoalApi,
        apiCallExecutor = apiCallExecutor,
        goalDao = goalDao,
        userProfileDao = userProfileDao,
    )

    private fun sampleGoal() = Goal(
        gender = "남",
        ageGroup = "20대",
        dailyGoalMinutes = 210,
        blockAfterGoal = true,
        apps = listOf(
            AppGoal("com.kakao.talk", "카카오톡", goalMinutes = 60, accessLimited = true),
            AppGoal("com.google.android.youtube", "YouTube", goalMinutes = 90, accessLimited = false),
        ),
    )

    // ── 저장 ────────────────────────────────────────────────────────

    @Test
    fun `서버가 실패해도 로컬 캐시에는 목표가 저장된다`() = runTest {
        monitoredAppApi.failWith = HttpException(errorResponse(401, "UNAUTHORIZED"))

        val result = repository.saveGoal(sampleGoal())

        assertTrue(result.isSuccess)
        assertEquals(210, goalDao.phoneGoal?.goalMinutes)
        assertEquals(true, goalDao.phoneGoal?.limitEnabled)
        assertEquals(2, goalDao.appGoals.size)
        assertEquals("남", userProfileDao.profile?.gender)
    }

    @Test
    fun `저장 성공 시 서버가 발급한 식별자가 로컬에 반영된다`() = runTest {
        val result = repository.saveGoal(sampleGoal())

        assertTrue(result.isSuccess)
        val kakao = goalDao.appGoals.first { it.packageName == "com.kakao.talk" }
        // packageName 이 키지만 서버 식별자도 함께 들고 있어야 다음 저장이 PATCH 로 나간다.
        assertEquals("monitored-com.kakao.talk", kakao.monitoredAppId)
        assertTrue(kakao.appGoalId!!.isNotEmpty())
        assertTrue(goalDao.phoneGoal?.serverGoalId!!.isNotEmpty())
    }

    @Test
    fun `선택에서 빠진 주의 앱은 서버에서 삭제된다`() = runTest {
        monitoredAppApi.apps += monitoredResponse("monitored-old", "com.old.app", "옛날앱", 0)

        repository.saveGoal(sampleGoal()).getOrThrow()

        assertEquals(listOf("monitored-old"), monitoredAppApi.deletedIds)
        assertTrue(monitoredAppApi.apps.none { it.packageName == "com.old.app" })
    }

    @Test
    fun `이미 있는 주의 앱은 다시 등록하지 않는다`() = runTest {
        monitoredAppApi.apps += monitoredResponse("monitored-kakao", "com.kakao.talk", "카카오톡", 0)

        repository.saveGoal(sampleGoal()).getOrThrow()

        // 카카오톡은 재사용, YouTube 만 새로 생성.
        assertEquals(listOf("com.google.android.youtube"), monitoredAppApi.createdPackages)
    }

    @Test
    fun `전체 목표가 이미 있으면 POST 대신 PATCH 로 보낸다`() = runTest {
        totalGoalApi.goal = TotalGoalResponse(
            id = "total-1", userId = "u", targetMinutes = 60, restrictAfter = false,
            createdAt = "", updatedAt = "",
        )

        repository.saveGoal(sampleGoal()).getOrThrow()

        assertEquals(0, totalGoalApi.createCount)
        assertEquals(1, totalGoalApi.updateCount)
        assertEquals(210, totalGoalApi.goal?.targetMinutes)
    }

    @Test
    fun `앱별 목표는 접근 제한과 목표 시간을 서버 필드로 매핑한다`() = runTest {
        repository.saveGoal(sampleGoal()).getOrThrow()

        val kakaoGoal = appGoalApi.goals.values.first { it.monitoredAppId == "monitored-com.kakao.talk" }
        assertEquals(60, kakaoGoal.targetMinutes)
        assertEquals(true, kakaoGoal.restrictAfter)
        // 입력 화면이 없어 최소값 1이 들어간다.
        assertEquals(1, kakaoGoal.targetCount)
    }

    @Test
    fun `목표 이유는 서버와 로컬 캐시에 함께 저장된다`() = runTest {
        val goal = sampleGoal().let { base ->
            base.copy(apps = base.apps.map { it.copy(goalReason = "쇼츠를 줄이기 위해") })
        }

        repository.saveGoal(goal).getOrThrow()

        val kakaoGoal = appGoalApi.goals.values.first { it.monitoredAppId == "monitored-com.kakao.talk" }
        assertEquals("쇼츠를 줄이기 위해", kakaoGoal.goalReason)
        // 설정(PREF)이 오프라인에서도 문구를 복원할 수 있어야 한다.
        assertEquals(
            "쇼츠를 줄이기 위해",
            goalDao.appGoals.first { it.packageName == "com.kakao.talk" }.goalReason,
        )
    }

    // ── 조회 ────────────────────────────────────────────────────────

    @Test
    fun `서버 목표를 도메인 모델로 합쳐 돌려주고 로컬에 미러링한다`() = runTest {
        userProfileDao.profile = UserProfileEntity(gender = "여", ageGroup = "30대")
        monitoredAppApi.apps += monitoredResponse("m-1", "com.kakao.talk", "카카오톡", 0)
        totalGoalApi.goal = TotalGoalResponse(
            id = "total-1", userId = "u", targetMinutes = 120, restrictAfter = true,
            createdAt = "", updatedAt = "",
        )
        appGoalApi.goals["m-1"] = AppGoalResponse(
            id = "ag-1", monitoredAppId = "m-1", targetMinutes = 45, targetCount = 3,
            restrictAfter = true, goalReason = null, createdAt = "", updatedAt = "",
        )

        val goal = repository.getGoal().getOrThrow()!!

        assertEquals(120, goal.dailyGoalMinutes)
        assertEquals(true, goal.blockAfterGoal)
        // 성별·나이는 서버 계약에 없어 로컬에서 합쳐진다.
        assertEquals("여", goal.gender)
        assertEquals(1, goal.apps.size)
        assertEquals("com.kakao.talk", goal.apps[0].packageName)
        assertEquals(45, goal.apps[0].goalMinutes)
        // 엔진이 오프라인에서 읽도록 캐시에 미러링돼야 한다.
        assertEquals(120, goalDao.phoneGoal?.goalMinutes)
        assertEquals("m-1", goalDao.appGoals.single().monitoredAppId)
    }

    @Test
    fun `서버 조회가 실패하면 로컬 캐시로 폴백한다`() = runTest {
        goalDao.phoneGoal = PhoneGoalEntity(goalMinutes = 99, limitEnabled = true)
        goalDao.appGoals += AppGoalEntity("com.kakao.talk", "카카오톡", 30, true)
        monitoredAppApi.failWith = HttpException(errorResponse(500, "INTERNAL_SERVER_ERROR"))

        val goal = repository.getGoal().getOrThrow()!!

        assertEquals(99, goal.dailyGoalMinutes)
        assertEquals(1, goal.apps.size)
    }

    @Test
    fun `서버에 목표가 없으면 로컬로 떨어지지 않고 null 을 준다`() = runTest {
        // 로컬에는 예전 값이 남아 있지만 서버가 "목표 없음"이라고 답한 상황.
        goalDao.phoneGoal = PhoneGoalEntity(goalMinutes = 99, limitEnabled = true)

        val goal = repository.getGoal().getOrThrow()

        assertNull(goal)
    }

    // ── 테스트 더블 ─────────────────────────────────────────────────

    private fun monitoredResponse(id: String, pkg: String, name: String, order: Int) =
        MonitoredAppResponse(
            id = id, userId = "u", packageName = pkg, appName = name,
            appIcon = null, sortOrder = order, createdAt = "", updatedAt = "",
        )

    private fun errorResponse(code: Int, errorCode: String) = Response.error<Any>(
        code,
        """{"success":false,"error":{"code":"$errorCode","message":"e"}}"""
            .toResponseBody("application/json".toMediaType()),
    )

    private class FakeMonitoredAppApi : MonitoredAppApi {
        val apps = mutableListOf<MonitoredAppResponse>()
        val deletedIds = mutableListOf<String>()
        val createdPackages = mutableListOf<String>()
        var failWith: Throwable? = null

        override suspend fun createMonitoredApp(
            request: MonitoredAppCreateRequest,
        ): ApiResponse<MonitoredAppResponse> {
            failWith?.let { throw it }
            createdPackages += request.packageName
            val created = MonitoredAppResponse(
                id = "monitored-${request.packageName}",
                userId = "u",
                packageName = request.packageName,
                appName = request.appName,
                appIcon = request.appIcon,
                sortOrder = request.sortOrder ?: apps.size,
                createdAt = "",
                updatedAt = "",
            )
            apps += created
            return ApiResponse(success = true, data = created)
        }

        override suspend fun getMonitoredApps(): ApiResponse<List<MonitoredAppResponse>> {
            failWith?.let { throw it }
            return ApiResponse(success = true, data = apps.toList())
        }

        override suspend fun getMonitoredApp(id: String): ApiResponse<MonitoredAppResponse> {
            failWith?.let { throw it }
            val found = apps.firstOrNull { it.id == id }
                ?: throw ApiExceptionFor(404, GoalErrorCodes.MONITORED_APP_NOT_FOUND)
            return ApiResponse(success = true, data = found)
        }

        override suspend fun updateMonitoredApp(
            id: String,
            request: MonitoredAppUpdateRequest,
        ): ApiResponse<MonitoredAppResponse> {
            failWith?.let { throw it }
            return ApiResponse(success = true, data = apps.first { it.id == id })
        }

        override suspend fun deleteMonitoredApp(id: String): Response<Unit> {
            failWith?.let { throw it }
            deletedIds += id
            apps.removeAll { it.id == id }
            return Response.success(null)
        }
    }

    private class FakeTotalGoalApi : TotalGoalApi {
        var goal: TotalGoalResponse? = null
        var createCount = 0
        var updateCount = 0

        override suspend fun createTotalGoal(
            request: TotalGoalCreateRequest,
        ): ApiResponse<TotalGoalResponse> {
            createCount++
            goal = TotalGoalResponse(
                id = "total-created", userId = "u",
                targetMinutes = request.targetMinutes,
                restrictAfter = request.restrictAfter ?: false,
                createdAt = "", updatedAt = "",
            )
            return ApiResponse(success = true, data = goal)
        }

        override suspend fun getTotalGoal(): ApiResponse<TotalGoalResponse> {
            val current = goal ?: throw ApiExceptionFor(404, GoalErrorCodes.TOTAL_GOAL_NOT_FOUND)
            return ApiResponse(success = true, data = current)
        }

        override suspend fun updateTotalGoal(
            request: TotalGoalUpdateRequest,
        ): ApiResponse<TotalGoalResponse> {
            updateCount++
            val current = goal ?: throw ApiExceptionFor(404, GoalErrorCodes.TOTAL_GOAL_NOT_FOUND)
            goal = current.copy(
                targetMinutes = request.targetMinutes ?: current.targetMinutes,
                restrictAfter = request.restrictAfter ?: current.restrictAfter,
            )
            return ApiResponse(success = true, data = goal)
        }
    }

    private class FakeAppGoalApi : AppGoalApi {
        // key = monitoredAppId
        val goals = mutableMapOf<String, AppGoalResponse>()

        override suspend fun createAppGoal(
            request: AppGoalCreateRequest,
        ): ApiResponse<AppGoalResponse> {
            val created = AppGoalResponse(
                id = "appgoal-${request.monitoredAppId}",
                monitoredAppId = request.monitoredAppId,
                targetMinutes = request.targetMinutes,
                targetCount = request.targetCount,
                restrictAfter = request.restrictAfter ?: false,
                goalReason = request.goalReason,
                createdAt = "", updatedAt = "",
            )
            goals[request.monitoredAppId] = created
            return ApiResponse(success = true, data = created)
        }

        override suspend fun getAppGoal(monitoredAppId: String): ApiResponse<AppGoalResponse> {
            val found = goals[monitoredAppId]
                ?: throw ApiExceptionFor(404, GoalErrorCodes.APP_GOAL_NOT_FOUND)
            return ApiResponse(success = true, data = found)
        }

        override suspend fun updateAppGoal(
            id: String,
            request: AppGoalUpdateRequest,
        ): ApiResponse<AppGoalResponse> {
            val key = goals.entries.first { it.value.id == id }.key
            val updated = goals.getValue(key).copy(
                targetMinutes = request.targetMinutes ?: goals.getValue(key).targetMinutes,
                targetCount = request.targetCount ?: goals.getValue(key).targetCount,
                restrictAfter = request.restrictAfter ?: goals.getValue(key).restrictAfter,
            )
            goals[key] = updated
            return ApiResponse(success = true, data = updated)
        }

        override suspend fun deleteAppGoal(id: String): Response<Unit> {
            goals.entries.removeAll { it.value.id == id }
            return Response.success(null)
        }
    }

    private class FakeGoalDao : GoalDao {
        var phoneGoal: PhoneGoalEntity? = null
        val appGoals = mutableListOf<AppGoalEntity>()

        override fun observePhoneGoal(): Flow<PhoneGoalEntity?> = flowOf(phoneGoal)
        override suspend fun getPhoneGoal(): PhoneGoalEntity? = phoneGoal
        override suspend fun upsertPhoneGoal(goal: PhoneGoalEntity) { phoneGoal = goal }
        override fun observeAppGoals(): Flow<List<AppGoalEntity>> = flowOf(appGoals.toList())
        override suspend fun getAppGoals(): List<AppGoalEntity> = appGoals.toList()
        override suspend fun upsertAppGoals(goals: List<AppGoalEntity>) {
            goals.forEach { goal ->
                appGoals.removeAll { it.packageName == goal.packageName }
                appGoals += goal
            }
        }
        override suspend fun deleteAppGoal(packageName: String) {
            appGoals.removeAll { it.packageName == packageName }
        }
        override suspend fun clearAppGoals() = appGoals.clear()
        override suspend fun findMonitoredAppId(packageName: String): String? =
            appGoals.firstOrNull { it.packageName == packageName }?.monitoredAppId
        override suspend fun findPackageName(monitoredAppId: String): String? =
            appGoals.firstOrNull { it.monitoredAppId == monitoredAppId }?.packageName
    }

    private class FakeUserProfileDao : UserProfileDao {
        var profile: UserProfileEntity? = null
        override suspend fun getProfile(): UserProfileEntity? = profile
        override suspend fun upsertProfile(profile: UserProfileEntity) { this.profile = profile }
    }
}

/** 공통 API 계층에서 HTTP 실패가 변환된 상황을 흉내내는 헬퍼. */
private fun ApiExceptionFor(status: Int, code: String) =
    ApiException.Http(
        statusCode = status,
        error = ApiError(code = code, message = code),
        cause = IllegalStateException(code),
    )

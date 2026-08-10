package com.phoneshim.android.data.repository

import com.google.gson.Gson
import com.phoneshim.android.data.api.MonitoredAppApi
import com.phoneshim.android.data.api.MonitoredAppCreateRequest
import com.phoneshim.android.data.api.MonitoredAppResponse
import com.phoneshim.android.data.api.MonitoredAppUpdateRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiResponse
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 주의 앱 조회와 packageName <-> monitoredAppId 변환 검증.
 * 차단 엔진·사용량 업로드가 오프라인에서도 변환할 수 있어야 하므로 캐시 우선 동작을 고정한다.
 */
class MonitoredAppRepositoryImplTest {

    private val api = FakeMonitoredAppApi()
    private val dao = FakeGoalDao()
    private val repository = MonitoredAppRepositoryImpl(
        monitoredAppApi = api,
        apiCallExecutor = ApiCallExecutor(Gson()),
        goalDao = dao,
    )

    private fun response(id: String, pkg: String, name: String) = MonitoredAppResponse(
        id = id, userId = "u", packageName = pkg, appName = name,
        appIcon = null, sortOrder = 0, createdAt = "", updatedAt = "",
    )

    private fun cached(pkg: String, label: String, monitoredAppId: String?) = AppGoalEntity(
        packageName = pkg, appLabel = label, goalMinutes = 60,
        limitEnabled = false, monitoredAppId = monitoredAppId,
    )

    @Test
    fun `서버 목록을 도메인 모델로 돌려준다`() = runTest {
        api.apps += response("m-1", "com.kakao.talk", "카카오톡")

        val apps = repository.getMonitoredApps().getOrThrow()

        assertEquals(1, apps.size)
        assertEquals("m-1", apps[0].id)
        assertEquals("com.kakao.talk", apps[0].packageName)
    }

    @Test
    fun `서버 조회가 실패하면 캐시로 답한다`() = runTest {
        api.failWith = IllegalStateException("network")
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")

        val apps = repository.getMonitoredApps().getOrThrow()

        assertEquals(1, apps.size)
        assertEquals("m-1", apps[0].id)
    }

    @Test
    fun `서버도 캐시도 없으면 실패를 올린다`() = runTest {
        api.failWith = IllegalStateException("network")

        assertTrue(repository.getMonitoredApps().isFailure)
    }

    @Test
    fun `packageName 변환은 캐시에서 먼저 찾고 서버를 부르지 않는다`() = runTest {
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")

        val id = repository.resolveMonitoredAppId("com.kakao.talk").getOrThrow()

        assertEquals("m-1", id)
        // 오프라인에서도 변환돼야 하므로 서버를 건드리면 안 된다.
        assertEquals(0, api.listCallCount)
    }

    @Test
    fun `캐시에 없으면 서버 목록으로 변환한다`() = runTest {
        api.apps += response("m-2", "com.google.android.youtube", "YouTube")

        val id = repository.resolveMonitoredAppId("com.google.android.youtube").getOrThrow()

        assertEquals("m-2", id)
        assertEquals(1, api.listCallCount)
    }

    @Test
    fun `등록되지 않은 앱은 null 을 준다`() = runTest {
        val id = repository.resolveMonitoredAppId("com.unknown.app").getOrThrow()

        assertNull(id)
    }

    @Test
    fun `monitoredAppId 를 packageName 으로 되돌린다`() = runTest {
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")

        val pkg = repository.resolvePackageName("m-1").getOrThrow()

        assertEquals("com.kakao.talk", pkg)
        assertEquals(0, api.listCallCount)
    }

    @Test
    fun `id 목록 변환은 캐시로 다 해결되면 서버를 부르지 않는다`() = runTest {
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")
        dao.appGoals += cached("com.google.android.youtube", "YouTube", "m-2")

        val packages = repository.resolvePackageNames(listOf("m-1", "m-2")).getOrThrow()

        assertEquals(listOf("com.kakao.talk", "com.google.android.youtube"), packages)
        assertEquals(0, api.listCallCount)
    }

    @Test
    fun `캐시에 없는 id 가 섞이면 서버를 한 번만 부른다`() = runTest {
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")
        api.apps += response("m-1", "com.kakao.talk", "카카오톡")
        api.apps += response("m-3", "com.instagram.android", "인스타그램")

        val packages = repository.resolvePackageNames(listOf("m-1", "m-3")).getOrThrow()

        assertEquals(listOf("com.kakao.talk", "com.instagram.android"), packages)
        assertEquals(1, api.listCallCount)
    }

    @Test
    fun `삭제된 주의 앱 id 는 결과에서 빠진다`() = runTest {
        dao.appGoals += cached("com.kakao.talk", "카카오톡", "m-1")
        api.apps += response("m-1", "com.kakao.talk", "카카오톡")

        // m-9 는 서버에도 캐시에도 없다(삭제됨).
        val packages = repository.resolvePackageNames(listOf("m-1", "m-9")).getOrThrow()

        assertEquals(listOf("com.kakao.talk"), packages)
    }

    @Test
    fun `빈 목록은 서버를 부르지 않고 빈 결과를 준다`() = runTest {
        val packages = repository.resolvePackageNames(emptyList()).getOrThrow()

        assertTrue(packages.isEmpty())
        assertEquals(0, api.listCallCount)
    }

    // ── 테스트 더블 ─────────────────────────────────────────────────

    private class FakeMonitoredAppApi : MonitoredAppApi {
        val apps = mutableListOf<MonitoredAppResponse>()
        var failWith: Throwable? = null
        var listCallCount = 0

        override suspend fun getMonitoredApps(): ApiResponse<List<MonitoredAppResponse>> {
            listCallCount++
            failWith?.let { throw it }
            return ApiResponse(success = true, data = apps.toList())
        }

        override suspend fun createMonitoredApp(
            request: MonitoredAppCreateRequest,
        ): ApiResponse<MonitoredAppResponse> = throw UnsupportedOperationException()

        override suspend fun getMonitoredApp(id: String): ApiResponse<MonitoredAppResponse> =
            throw UnsupportedOperationException()

        override suspend fun updateMonitoredApp(
            id: String,
            request: MonitoredAppUpdateRequest,
        ): ApiResponse<MonitoredAppResponse> = throw UnsupportedOperationException()

        override suspend fun deleteMonitoredApp(id: String) = throw UnsupportedOperationException()
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
}

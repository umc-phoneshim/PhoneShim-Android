package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.ApiException
import com.phoneshim.android.data.api.AppGoalApi
import com.phoneshim.android.data.api.AppGoalCreateRequest
import com.phoneshim.android.data.api.AppGoalResponse
import com.phoneshim.android.data.api.AppGoalUpdateRequest
import com.phoneshim.android.data.api.GoalErrorCodes
import com.phoneshim.android.data.api.MonitoredAppApi
import com.phoneshim.android.data.api.MonitoredAppCreateRequest
import com.phoneshim.android.data.api.MonitoredAppResponse
import com.phoneshim.android.data.api.TotalGoalApi
import com.phoneshim.android.data.api.TotalGoalCreateRequest
import com.phoneshim.android.data.api.TotalGoalResponse
import com.phoneshim.android.data.api.TotalGoalUpdateRequest
import com.phoneshim.android.data.api.runCatchingApi
import com.phoneshim.android.data.api.toApiException
import com.phoneshim.android.data.api.unwrap
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity
import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.repository.GoalRepository
import javax.inject.Inject
import retrofit2.HttpException

/**
 * 목표 저장소. 서버 MonitoredApp/TotalGoal/AppGoal 3개 도메인을 도메인 모델 [Goal] 하나로 합쳐 다룬다.
 *
 * [packageName ↔ monitoredAppId]
 *   서버는 앱을 monitoredAppId(uuid)로 식별하지만 차단 엔진은 packageName 으로 감지한다.
 *   그래서 로컬 캐시는 packageName 을 키로 두고, 서버 식별자는 부가 컬럼으로 함께 저장한다.
 *   다음 저장 때 이 식별자가 있으면 POST 대신 PATCH 로 보내 409 중복을 피한다.
 *
 * [저장 순서]
 *   로컬 우선. 차단 엔진 판정의 실제 소스가 로컬 캐시라, 네트워크 실패가 온보딩 완료와
 *   엔진 동작을 막지 않아야 한다. 서버 동기화는 best-effort 로 뒤따른다.
 *   (인증 토큰 연결 전까지는 서버 호출이 401 로 끝나므로 사실상 로컬만 동작한다.)
 */
class GoalRepositoryImpl @Inject constructor(
    private val monitoredAppApi: MonitoredAppApi,
    private val totalGoalApi: TotalGoalApi,
    private val appGoalApi: AppGoalApi,
    private val goalDao: GoalDao,
    private val userProfileDao: UserProfileDao,
) : GoalRepository {

    override suspend fun getGoal(): Result<Goal?> = runCatching {
        // 서버(원본) 우선, 네트워크 실패·미인증이면 로컬 캐시로 폴백.
        // 조회에 성공했는데 목표가 없는 것(null)과 조회 자체가 실패한 것을 구분한다.
        // 전자는 그대로 null(온보딩 필요), 후자만 로컬 캐시로 떨어진다.
        val remote = fetchRemoteGoal()
        if (remote.isSuccess) remote.getOrNull() else getLocalGoal()
    }

    override suspend fun saveGoal(goal: Goal): Result<Unit> = runCatching {
        saveLocal(goal)
        // 서버 동기화 실패가 로컬 저장을 되돌리지 않는다.
        runCatchingApi { syncToServer(goal) }
        Unit
    }

    // ── 조회 ────────────────────────────────────────────────────────

    /**
     * 서버에서 주의 앱·전체 목표·앱별 목표를 모아 [Goal] 로 만든다.
     * 성공했지만 아직 설정된 목표가 없으면 성공(null)이고, 통신 자체가 실패하면 failure 다.
     */
    private suspend fun fetchRemoteGoal(): Result<Goal?> = runCatchingApi {
        val monitoredApps = monitoredAppApi.getMonitoredApps().unwrap()
        val totalGoal = getTotalGoalOrNull()
        // 목표를 아직 한 번도 설정하지 않은 상태. 온보딩을 띄워야 하므로 null.
        if (monitoredApps.isEmpty() && totalGoal == null) {
            return@runCatchingApi null
        }

        val appGoals = monitoredApps.associate { app -> app.id to getAppGoalOrNull(app.id) }
        val goal = toDomain(monitoredApps, totalGoal, appGoals)

        // 서버가 원본이므로 성공적으로 읽었으면 엔진용 캐시를 갱신해 둔다.
        mirrorToLocal(monitoredApps, totalGoal, appGoals)
        goal
    }

    /** 차단 엔진 캐시에서 목표를 복원. 저장된 게 없으면 null. */
    private suspend fun getLocalGoal(): Goal? {
        val phone = goalDao.getPhoneGoal()
        val apps = goalDao.getAppGoals()
        if (phone == null && apps.isEmpty()) return null
        val profile = userProfileDao.getProfile()
        return Goal(
            id = phone?.serverGoalId,
            gender = profile?.gender,
            ageGroup = profile?.ageGroup,
            dailyGoalMinutes = phone?.goalMinutes ?: 0,
            blockAfterGoal = phone?.limitEnabled ?: false,
            apps = apps.map {
                AppGoal(
                    packageName = it.packageName,
                    appName = it.appLabel,
                    goalMinutes = it.goalMinutes,
                    accessLimited = it.limitEnabled,
                    targetCount = it.targetCount,
                )
            },
        )
    }

    private suspend fun toDomain(
        monitoredApps: List<MonitoredAppResponse>,
        totalGoal: TotalGoalResponse?,
        appGoals: Map<String, AppGoalResponse?>,
    ): Goal {
        // 성별·나이는 서버 계약에 없어 로컬이 유일한 저장처다.
        val profile = userProfileDao.getProfile()
        return Goal(
            id = totalGoal?.id,
            gender = profile?.gender,
            ageGroup = profile?.ageGroup,
            dailyGoalMinutes = totalGoal?.targetMinutes ?: 0,
            blockAfterGoal = totalGoal?.restrictAfter ?: false,
            apps = monitoredApps.map { app ->
                val appGoal = appGoals[app.id]
                AppGoal(
                    packageName = app.packageName,
                    appName = app.appName,
                    goalMinutes = appGoal?.targetMinutes ?: 0,
                    accessLimited = appGoal?.restrictAfter ?: false,
                    targetCount = appGoal?.targetCount ?: 1,
                )
            },
        )
    }

    // ── 저장 ────────────────────────────────────────────────────────

    private suspend fun saveLocal(goal: Goal) {
        // 기존에 저장해 둔 서버 식별자는 유지해야 다음 동기화가 PATCH 로 나간다.
        val previousApps = goalDao.getAppGoals().associateBy { it.packageName }
        val previousPhone = goalDao.getPhoneGoal()

        goalDao.upsertPhoneGoal(
            PhoneGoalEntity(
                goalMinutes = goal.dailyGoalMinutes,
                limitEnabled = goal.blockAfterGoal,
                serverGoalId = goal.id ?: previousPhone?.serverGoalId,
            ),
        )
        // 온보딩 저장 = 주의앱 목록 전체 교체(재설정 시 해제된 앱 제거).
        goalDao.clearAppGoals()
        goalDao.upsertAppGoals(
            goal.apps.map { app ->
                val previous = previousApps[app.packageName]
                AppGoalEntity(
                    packageName = app.packageName,
                    appLabel = app.appName,
                    goalMinutes = app.goalMinutes,
                    limitEnabled = app.accessLimited,
                    targetCount = app.targetCount,
                    monitoredAppId = previous?.monitoredAppId,
                    appGoalId = previous?.appGoalId,
                )
            },
        )
        // 성별·나이는 엔진 판정에 안 쓰이지만 온보딩 재진입 시 복원해야 하므로 함께 남긴다.
        userProfileDao.upsertProfile(
            UserProfileEntity(gender = goal.gender, ageGroup = goal.ageGroup),
        )
    }

    /**
     * 서버 상태를 [goal] 에 맞춘다.
     * 선택에서 빠진 주의 앱은 삭제하고(연결된 app_goals 는 서버 cascade),
     * 남은 앱은 등록/갱신한 뒤 전체 목표와 앱별 목표를 upsert 한다.
     */
    private suspend fun syncToServer(goal: Goal) {
        val remoteApps = monitoredAppApi.getMonitoredApps().unwrap()
        val remoteByPackage = remoteApps.associateBy { it.packageName }
        val selectedPackages = goal.apps.mapTo(mutableSetOf()) { it.packageName }

        remoteApps
            .filter { it.packageName !in selectedPackages }
            .forEach { monitoredAppApi.deleteMonitoredApp(it.id) }

        val totalGoal = upsertTotalGoal(goal)

        val syncedApps = goal.apps.mapIndexed { index, app ->
            val monitored = remoteByPackage[app.packageName]
                ?: monitoredAppApi.createMonitoredApp(
                    MonitoredAppCreateRequest(
                        packageName = app.packageName,
                        appName = app.appName,
                        sortOrder = index,
                    ),
                ).unwrap()
            val appGoal = upsertAppGoal(monitored.id, app)
            app to (monitored to appGoal)
        }

        // 서버가 발급한 식별자를 캐시에 반영해 둔다.
        goalDao.upsertPhoneGoal(
            PhoneGoalEntity(
                goalMinutes = goal.dailyGoalMinutes,
                limitEnabled = goal.blockAfterGoal,
                serverGoalId = totalGoal.id,
            ),
        )
        goalDao.upsertAppGoals(
            syncedApps.map { (app, ids) ->
                val (monitored, appGoal) = ids
                AppGoalEntity(
                    packageName = app.packageName,
                    appLabel = app.appName,
                    goalMinutes = app.goalMinutes,
                    limitEnabled = app.accessLimited,
                    targetCount = app.targetCount,
                    monitoredAppId = monitored.id,
                    appGoalId = appGoal.id,
                )
            },
        )
    }

    /** 전체 목표는 사용자당 1개다. 없으면 POST, 있으면 PATCH. */
    private suspend fun upsertTotalGoal(goal: Goal): TotalGoalResponse {
        val existing = getTotalGoalOrNull()
        return if (existing == null) {
            totalGoalApi.createTotalGoal(
                TotalGoalCreateRequest(
                    targetMinutes = goal.dailyGoalMinutes,
                    restrictAfter = goal.blockAfterGoal,
                ),
            ).unwrap()
        } else {
            totalGoalApi.updateTotalGoal(
                TotalGoalUpdateRequest(
                    targetMinutes = goal.dailyGoalMinutes,
                    restrictAfter = goal.blockAfterGoal,
                ),
            ).unwrap()
        }
    }

    /** 주의 앱 1개당 목표 1개다. 없으면 POST, 있으면 PATCH. */
    private suspend fun upsertAppGoal(monitoredAppId: String, app: AppGoal): AppGoalResponse {
        val existing = getAppGoalOrNull(monitoredAppId)
        return if (existing == null) {
            appGoalApi.createAppGoal(
                AppGoalCreateRequest(
                    monitoredAppId = monitoredAppId,
                    targetMinutes = app.goalMinutes,
                    targetCount = app.targetCount,
                    restrictAfter = app.accessLimited,
                ),
            ).unwrap()
        } else {
            appGoalApi.updateAppGoal(
                existing.id,
                AppGoalUpdateRequest(
                    targetMinutes = app.goalMinutes,
                    targetCount = app.targetCount,
                    restrictAfter = app.accessLimited,
                ),
            ).unwrap()
        }
    }

    // ── 로컬 미러링 ─────────────────────────────────────────────────

    private suspend fun mirrorToLocal(
        monitoredApps: List<MonitoredAppResponse>,
        totalGoal: TotalGoalResponse?,
        appGoals: Map<String, AppGoalResponse?>,
    ) {
        goalDao.upsertPhoneGoal(
            PhoneGoalEntity(
                goalMinutes = totalGoal?.targetMinutes ?: 0,
                limitEnabled = totalGoal?.restrictAfter ?: false,
                serverGoalId = totalGoal?.id,
            ),
        )
        goalDao.clearAppGoals()
        goalDao.upsertAppGoals(
            monitoredApps.map { app ->
                val appGoal = appGoals[app.id]
                AppGoalEntity(
                    packageName = app.packageName,
                    appLabel = app.appName,
                    goalMinutes = appGoal?.targetMinutes ?: 0,
                    limitEnabled = appGoal?.restrictAfter ?: false,
                    targetCount = appGoal?.targetCount ?: 1,
                    monitoredAppId = app.id,
                    appGoalId = appGoal?.id,
                )
            },
        )
    }

    // ── 404 = "아직 없음" ───────────────────────────────────────────
    // 전체 목표·앱 목표가 없는 건 오류가 아니라 미설정 상태다. 그 404 만 null 로 바꾸고
    // 나머지 오류(401, 5xx 등)는 그대로 올려 상위에서 폴백/표시하게 둔다.

    private suspend fun getTotalGoalOrNull(): TotalGoalResponse? =
        nullOnNotFound(GoalErrorCodes.TOTAL_GOAL_NOT_FOUND) {
            totalGoalApi.getTotalGoal().unwrap()
        }

    private suspend fun getAppGoalOrNull(monitoredAppId: String): AppGoalResponse? =
        nullOnNotFound(GoalErrorCodes.APP_GOAL_NOT_FOUND) {
            appGoalApi.getAppGoal(monitoredAppId).unwrap()
        }

    private suspend fun <T> nullOnNotFound(code: String, block: suspend () -> T): T? = try {
        block()
    } catch (e: HttpException) {
        // Retrofit 은 HTTP 오류를 HttpException 으로 던진다. 서버 에러 코드를 보려면 변환이 필요하다.
        val api = e.toApiException()
        if (api.httpStatus == 404 && api.code == code) null else throw api
    } catch (e: ApiException) {
        // envelope 의 success=false 로 온 경우.
        if (e.code == code) null else throw e
    }
}

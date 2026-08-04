package com.phoneshim.android.blocking.upload

import android.util.Log
import com.phoneshim.android.blocking.detection.AppUsageSnapshot
import com.phoneshim.android.blocking.detection.BlockedInterval
import com.phoneshim.android.blocking.detection.UsageMinutesReader
import com.phoneshim.android.blocking.policy.BlockingPolicyProvider
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.usecase.UploadDeviceUsageUseCase
import com.phoneshim.android.domain.usecase.UploadUsageLogUseCase
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 한 번의 업로드로 보낼 값. 날짜는 기기 자정 기준(이슈 #45). */
data class UsageUploadPayload(
    val date: String,
    val phoneMinutes: Int,
    val apps: List<AppUsageSnapshot>,
)

/**
 * 사용량 서버 업로드.
 *
 * 네트워크 코드는 폴님 소유(분배안 5-2)라 use case 만 호출한다.
 * 이 클래스는 "언제, 어떤 값을" 보낼지와 실패 시 보존만 담당한다.
 *
 * 재시도 정책을 따로 만들지 않는 이유는 서버 upsert 가 누적값 덮어쓰기이기 때문이다.
 * 하루 중의 실패는 다음 주기가 흡수하고, 유실되는 건 그 날짜의 마지막 전송뿐이라
 * 그것만 보존했다가 date 를 명시해 다시 보낸다. (이슈 #45)
 */
@Singleton
class UsageUploader @Inject constructor(
    private val usageReader: UsageMinutesReader,
    private val policyProvider: BlockingPolicyProvider,
    private val uploadUsageLog: UploadUsageLogUseCase,
    private val uploadDeviceUsage: UploadDeviceUsageUseCase,
    private val pendingStore: PendingUsageUploadStore,
) {
    // 주기 루프와 SCREEN_OFF 밀어주기가 겹칠 수 있다.
    private val mutex = Mutex()

    /** 마지막으로 서버에 올린 값. 같은 값이면 다시 보내지 않는다. */
    private var lastSent: UsageUploadPayload? = null

    /**
     * 지금 시점 사용량을 올린다.
     *
     * @param blockedIntervals 차단 중이던 구간. 판정에 쓰는 값과 같은 소스를 써야
     *   화면과 서버 숫자가 어긋나지 않는다.
     * @param foregroundPackage 자정 이전부터 이어지는 세션 보정용.
     */
    suspend fun upload(
        blockedIntervals: List<BlockedInterval>,
        foregroundPackage: String?,
    ) = mutex.withLock {
        val today = LocalDate.now().toString()

        // 지난 날짜 보전 전송 먼저. 오늘치는 아래에서 어차피 다시 보낸다.
        pendingStore.pending(exceptDate = today).forEach { old ->
            if (send(old)) pendingStore.remove(old.date)
        }

        val watched = runCatching { policyProvider.watchedApps() }
            .getOrElse {
                Log.w(TAG, "주의앱 목록 조회 실패. 이번 주기 건너뜀", it)
                return@withLock
            }
            .map { it.packageName }
            .toSet()

        val snapshot = usageReader.usageSnapshot(
            // 단일 앱 값은 여기서 안 쓴다. 전체폰과 주의앱별 목록만 필요.
            packageName = foregroundPackage.orEmpty(),
            blockedIntervals = blockedIntervals,
            foregroundPackage = foregroundPackage,
            watchedPackages = watched,
        )

        val payload = UsageUploadPayload(
            date = today,
            phoneMinutes = snapshot.phoneMinutes,
            apps = snapshot.watchedApps,
        )
        // 값이 그대로면 보낼 이유가 없다. 화면이 꺼져 있으면 사용량도 늘지 않는다.
        if (payload == lastSent) return@withLock

        // 보내기 전에 남긴다. 전송 중 프로세스가 죽어도 이 값이 남아야 보전 전송이 된다.
        pendingStore.save(payload)
        if (send(payload)) {
            pendingStore.remove(today)
            lastSent = payload
        }
    }

    /** 자정 롤오버. 어제 값과 비교해 스킵하는 일이 없게 한다. */
    fun onDayRollover() {
        lastSent = null
    }

    /** @return 재시도할 필요가 없을 만큼 끝났는가. 네트워크 실패가 하나라도 있으면 false. */
    private suspend fun send(payload: UsageUploadPayload): Boolean {
        var retryable = false

        uploadDeviceUsage(payload.phoneMinutes, payload.date)
            .onFailure { retryable = retryable or handleFailure("device-usage", payload.date, it) }

        payload.apps.forEach { app ->
            uploadUsageLog(app.packageName, app.usedMinutes, app.entryCount, payload.date)
                .onFailure { retryable = retryable or handleFailure(app.packageName, payload.date, it) }
        }
        return !retryable
    }

    /**
     * @return 보존 후 재전송할 가치가 있는 실패인가.
     *
     * 네트워크 실패만 양성 판별하면 된다. 매핑 실패(등록 안 된 주의앱), 4xx, 파싱 실패는
     * 같은 값을 다시 보내도 결과가 같으므로 보존하지 않고 넘긴다.
     */
    private fun handleFailure(target: String, date: String, e: Throwable): Boolean {
        val retryable = e is ApiException.Network
        if (retryable) {
            Log.i(TAG, "업로드 보류(네트워크): $target $date")
        } else {
            Log.w(TAG, "업로드 실패(재전송 안 함): $target $date", e)
        }
        return retryable
    }

    private companion object {
        const val TAG = "UsageUploader"
    }
}
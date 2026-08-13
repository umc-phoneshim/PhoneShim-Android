package com.phoneshim.android.blocking.schedule

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.phoneshim.android.blocking.detection.BlockingPermissions
import com.phoneshim.android.blocking.service.BlockerService
import com.phoneshim.android.domain.schedule.ReminderScheduleCoordinator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [ReminderScheduleCoordinator] 엔진 구현.
 *
 * 호출자(리마인더 Repository)는 도메인 인터페이스만 알고, AlarmManager 나 BlockerService 는 모른다.
 *
 * ## 왜 단건 예약이 없는가
 *
 * [ReminderAlarmScheduler.rescheduleToday] 가 `FLAG_UPDATE_CURRENT` 로 알람을 덮어쓰므로
 * 오늘 전체를 다시 예약해도 결과가 같다. 그래서 taskId 단건 조회 DAO 를 따로 두지 않고
 * "취소 + 오늘 전체 재예약" 조합으로 네 메서드를 모두 표현한다.
 * 오늘 리마인더는 많아야 몇 건이라 비용도 무시할 수 있다.
 */
@Singleton
class ReminderScheduleCoordinatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduler: ReminderAlarmScheduler,
) : ReminderScheduleCoordinator {

    // CRUD 가 연달아 일어나면 재예약이 겹칠 수 있다.
    private val mutex = Mutex()

    override suspend fun schedule(taskId: String) = refreshToday()

    override suspend fun reschedule(taskId: String) = mutex.withLock {
        // 시각이 바뀌었거나 제한 모드가 NONE 이 됐을 수 있으므로 먼저 지운다.
        scheduler.cancel(taskId)
        runCatching { scheduler.rescheduleToday() }
            .onFailure { Log.w(TAG, "재예약 실패: $taskId", it) }
        Unit
    }

    override suspend fun cancel(taskId: String) = mutex.withLock {
        scheduler.cancel(taskId)
        // 그 일정 때문에 지금 제한이 걸려 있었다면 즉시 풀어야 한다(명세 요구사항).
        // 캐시에서 이미 지워진 뒤이므로, 재판정하면 일정 차단이 사라져 오버레이가 내려간다.
        requestReevaluate()
    }

    override suspend fun refreshToday() = mutex.withLock {
        runCatching { scheduler.rescheduleToday() }
            .onFailure { Log.w(TAG, "오늘 일정 재예약 실패", it) }
        Unit
    }

    /**
     * 서비스에 재판정을 요청한다.
     *
     * 알람이 깨울 때와 같은 경로([BlockerService.ACTION_REEVALUATE])를 쓴다.
     * 권한이 없으면 서비스가 애초에 뜨지 않으므로 시도하지 않는다.
     */
    private fun requestReevaluate() {
        if (!BlockingPermissions.hasAll(context)) return
        runCatching {
            val intent = Intent(context, BlockerService::class.java).apply {
                action = BlockerService.ACTION_REEVALUATE
            }
            ContextCompat.startForegroundService(context, intent)
        }.onFailure { Log.w(TAG, "재판정 요청 실패", it) }
    }

    private companion object {
        const val TAG = "ReminderSchedule"
    }
}
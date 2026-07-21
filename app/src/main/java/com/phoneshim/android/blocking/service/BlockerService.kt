package com.phoneshim.android.blocking.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.phoneshim.android.blocking.detection.ForegroundAppDetector
import com.phoneshim.android.blocking.detection.UsageMinutesReader
import com.phoneshim.android.blocking.overlay.BlockOverlayManager
import com.phoneshim.android.blocking.overlay.OverlayAction
import com.phoneshim.android.blocking.policy.BlockDecision
import com.phoneshim.android.blocking.policy.BlockPolicyEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 포그라운드 서비스. 화면이 켜져 있을 때만 짧은 주기로 포그라운드 앱을 확인해 판정한다.
 * 화면 꺼짐 시 폴 루프를 멈춰 배터리 부하를 없앤다.
 *   리마인더 일정 차단은 폴링이 아니라 AlarmManager 예약이 담당하므로,
 *   화면이 꺼져 있어도 예약된 시각에 깨어나 동작한다.
 */
@AndroidEntryPoint
class BlockerService : Service() {

    @Inject lateinit var detector: ForegroundAppDetector
    @Inject lateinit var usageReader: UsageMinutesReader
    @Inject lateinit var engine: BlockPolicyEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var overlay: BlockOverlayManager
    private lateinit var powerManager: PowerManager

    // tick() 동시 실행 방지.
    // 폴 루프와 알람 REEVALUATE 가 같은 scope에서 tick 을 돌릴 수 있는데,
    // detector 커서, 세션 필드가 단일 스레드 전제라 동시 실행 시 레이스가 난다.
    private val tickMutex = Mutex()

    // 화면 ON/OFF 에 따라 폴 루프를 껐다 켠다.
    @Volatile private var screenOn: Boolean = true
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    screenOn = true
                    detector.reset()
                    scope.launch { tick() }  // 다음 폴 안 기다리고 즉시 재판정
                }
                Intent.ACTION_SCREEN_OFF -> {
                    screenOn = false
                    // 화면 꺼지면 차단 화면도 의미 없으니 내림
                    scope.launch(Dispatchers.Main) { overlay.hide() }
                }
            }
        }
    }

    // 사유 프롬프트 중복 방지.
    // FRD REP-01: "앱 종료 후 1분 내 재진입 시 사유 재입력 안 함".
    // 순수 타이머(#6)가 아니라 "포그라운드 세션"을 기준으로 판단한다:
    //   같은 앱을 계속 쓰는 동안엔 다시 안 물음(세션 유지).
    //   다른 앱으로 나갔다가 1분 내 돌아오면 안 물음(쿨다운).
    //   1분 넘겨 돌아오면 다시 물음.
    private var reasonAskedForPackage: String? = null // 현재 세션에서 이미 물어본 앱
    private var lastForegroundPackage: String? = null // 직전 tick 의 포그라운드
    private var lastExitedPackage: String? = null     // 마지막으로 벗어난 앱
    private var lastExitAtMs: Long = 0L

    // #4: "알림만"(차단 OFF) 목표 도달 화면은 Dismiss 후 그날 재출현 안 함.
    // 자정 넘어가면 초기화. 앱별(패키지) + 전체 폰(KEY_PHONE) 각각.
    private val dismissedGoalToday = mutableSetOf<String>()
    private var notifiedDayEpoch: Long = -1L
    private var showingGoalKey: String? = null // 현재 떠 있는 목표-도달 화면의 키(Dismiss 시 기록용)

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        screenOn = powerManager.isInteractive
        overlay = BlockOverlayManager(this, ::handleOverlayAction)
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
        )
        startAsForeground()
        loop()
    }

    private fun loop() = scope.launch {
        while (isActive) {
            if (screenOn) {
                tick()
                delay(POLL_INTERVAL_MS)
            } else {
                // 화면 꺼짐: 폴링 정지. 켜질 때까지 느리게 대기만.
                delay(SCREEN_OFF_IDLE_MS)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 리마인더 알람이 깨운 경우: 즉시 1회 재판정
        if (intent?.action == ACTION_REEVALUATE) {
            scope.launch { if (screenOn) tick() }
        }
        return START_STICKY
    }

    /** 폴 루프와 알람 REEVALUATE 양쪽에서 부르므로 mutex 로 동시 실행을 막는다. */
    private suspend fun tick() = tickMutex.withLock { runTick() }

    private suspend fun runTick() {
        val pkg = detector.currentForegroundPackage() ?: return

        // 포그라운드 전환 감지 → 세션 경계 갱신
        if (pkg != lastForegroundPackage) {
            lastForegroundPackage?.let {
                lastExitedPackage = it
                lastExitAtMs = System.currentTimeMillis()
            }
            // 새 앱으로 바뀌면, 그 앱에 대한 "이미 물어봤음"을 초기화
            // (단, 1분 내 재진입이면 아래 asked 로 다시 안 물음)
            if (pkg != reasonAskedForPackage) reasonAskedForPackage = null
            lastForegroundPackage = pkg
        }

        val phoneUsed = usageReader.usedMinutesToday(null)
        val appUsed = usageReader.usedMinutesToday(pkg)

        // 이미 이번 세션에 물었거나 / 방금 나갔다 1분 내 재진입이면 스킵
        val recentlyReentered = pkg == lastExitedPackage &&
            System.currentTimeMillis() - lastExitAtMs < REASON_COOLDOWN_MS
        val asked = pkg == reasonAskedForPackage || recentlyReentered

        val decision = engine.decide(pkg, phoneUsed, appUsed, asked)

        // #4: 차단 OFF 목표 도달(알림만)은 Dismiss 후 그날 재출현 안 함.
        val suppressed = suppressIfAlreadyNotified(decision)

        // 지금 떠 있는 게 목표-도달 알림이면 그 키를 기억(Dismiss 시 기록용)
        showingGoalKey = when (suppressed) {
            BlockDecision.PhoneGoalReached -> KEY_PHONE
            is BlockDecision.AppGoalReached -> suppressed.packageName
            else -> null
        }

        withContext(Dispatchers.Main) {
            when (suppressed) {
                BlockDecision.Allow -> overlay.hide()
                else -> overlay.show(suppressed)
            }
        }
    }

    /**
     * PhoneGoalReached / AppGoalReached 는 사용량 조건이 계속 참이라 매 tick 반환된다.
     * 사용자가 Dismiss(확인) 하기 전까지는 계속 보여주되(같은 화면이라 재생성 안 됨),
     * 한 번 Dismiss 하면 그날은 다시 안 뜨게 한다. 그래서 여기선 "이미 dismiss 됐나"만 본다.
     * (차단 ON 인 PhoneBlocked/AppBlocked 는 억제 대상 아님.)
     */
    private fun suppressIfAlreadyNotified(decision: BlockDecision): BlockDecision {
        rolloverDayIfNeeded()
        val key = when (decision) {
            BlockDecision.PhoneGoalReached -> KEY_PHONE
            is BlockDecision.AppGoalReached -> decision.packageName
            else -> return decision
        }
        return if (key in dismissedGoalToday) BlockDecision.Allow else decision
    }

    private fun rolloverDayIfNeeded() {
        val today = java.time.LocalDate.now().toEpochDay()
        if (today != notifiedDayEpoch) {
            notifiedDayEpoch = today
            dismissedGoalToday.clear()
        }
    }

    private fun handleOverlayAction(action: OverlayAction) {
        when (action) {
            OverlayAction.Dismiss -> {
                // 목표 도달 알림(37/39)을 확인한 거면 그날 다시 안 뜨게 기록
                showingGoalKey?.let { dismissedGoalToday.add(it) }
                showingGoalKey = null
                overlay.hide()
            }
            OverlayAction.OpenPhoneShim -> {
                overlay.hide()
                packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launch)
                }
            }
            OverlayAction.Call -> {
                overlay.hide()
                launchDefaultApp(Intent.ACTION_DIAL)
            }
            OverlayAction.Message -> {
                overlay.hide()
                // 기본 SMS 앱 실행
                launchDefaultApp(Intent.ACTION_MAIN, category = Intent.CATEGORY_APP_MESSAGING)
            }
            is OverlayAction.ReasonSubmitted -> {
                // #4: detector 재조회 금지. 프롬프트가 들고 있던 패키지를 그대로 사용.
                reasonAskedForPackage = action.packageName
                // TODO(report): action.reason 저장
                overlay.hide()
            }
        }
    }

    private fun launchDefaultApp(action: String, category: String? = null) {
        val intent = Intent(action).apply {
            category?.let { addCategory(it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
    }

    private fun startAsForeground() {
        val notification = BlockerNotification.build(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                BlockerNotification.ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(BlockerNotification.ID, notification)
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        scope.cancel()
        if (::overlay.isInitialized) overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_REEVALUATE = "com.phoneshim.android.action.REEVALUATE"
        private const val POLL_INTERVAL_MS = 1_000L
        private const val SCREEN_OFF_IDLE_MS = 60_000L
        private const val REASON_COOLDOWN_MS = 60_000L
        private const val KEY_PHONE = "__PHONE__"
    }
}

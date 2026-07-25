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
import android.provider.Telephony
import android.telecom.TelecomManager
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 포그라운드 서비스. 화면이 켜져 있을 때만 짧은 주기로 포그라운드 앱을 확인해 판정.
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
    // 화면 on/off 상태. 폴 루프가 이 값을 기다리기 때문에 단순 Boolean 이 아니라 StateFlow.
    // 고정 시간 대기(delay)로 두면 화면이 켜져도 그 대기가 안 끊겨서 최대 대기시간만큼
    // 감시가 멈춤.
    private val screenOnFlow = MutableStateFlow(true)
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    screenOnFlow.value = true
                    detector.reset()
                    scope.launch { tick() }  // 다음 폴 안 기다리고 즉시 재판정
                }
                Intent.ACTION_SCREEN_OFF -> {
                    screenOnFlow.value = false
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

    // 전화/문자/폰쉼 진입으로 앱을 띄운 직후, 대상 앱이 포그라운드로 올라올 때까지
    // 이전 차단 앱 기준 재차단 오버레이가 번쩍이지 않게 잠깐 억제하는 시각.
    @Volatile private var overlaySuppressedUntilMs: Long = 0L

    // 지금 떠 있는 화면이 '앱 하드 차단'인가(= 확인 누르면 홈으로 보낼 대상인가).
    private var showingAppBlock: Boolean = false

    // 하드 차단이 시작된 시각(0 = 차단 아님). 이 이후의 시간은 사용량 집계에서 제외한다.
    private var blockActiveSinceMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        screenOnFlow.value = powerManager.isInteractive
        overlay = BlockOverlayManager(this, ::handleOverlayAction)
        engine.extraAllowed = resolveEssentialAllowed()
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
            if (screenOnFlow.value) {
                tick()
                delay(POLL_INTERVAL_MS)
            } else {
                // 화면 꺼짐: 폴링 완전 정지. 깨어나는 건 '켜짐 신호'를 받는 순간이라
                // 고정 대기와 달리 재개가 지연되지 않는다(배터리도 이쪽이 더 낫다: 대기 중 깨어남 0회).
                screenOnFlow.first { it }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 리마인더 알람이 깨운 경우: 즉시 1회 재판정
        if (intent?.action == ACTION_REEVALUATE) {
            scope.launch { if (screenOnFlow.value) tick() }
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

        // 차단이 떠 있는 동안은 '차단 시작 시각'을 상한으로 줘서, 막힌 시간이 사용량에
        // 쌓이지 않게 한다(못 썼는데 목표를 깎아먹는 것 방지). 차단이 아니면 now(=실시간).
        val ceiling = if (blockActiveSinceMs != 0L) blockActiveSinceMs else System.currentTimeMillis()
        val phoneUsed = usageReader.usedMinutesToday(null, ceiling)
        val appUsed = usageReader.usedMinutesToday(pkg, ceiling)

        // 이미 이번 세션에 물었거나 / 방금 나갔다 1분 내 재진입이면 스킵
        val recentlyReentered = pkg == lastExitedPackage &&
                System.currentTimeMillis() - lastExitAtMs < REASON_COOLDOWN_MS
        val asked = pkg == reasonAskedForPackage || recentlyReentered

        val decision = engine.decide(pkg, phoneUsed, appUsed, asked)
            .let(::withResolvedLabel)

        // #4: 차단 OFF 목표 도달(알림만)은 Dismiss 후 그날 재출현 안 함.
        val suppressed = suppressIfAlreadyNotified(decision)

        // 지금 떠 있는 게 목표-도달 알림이면 그 키를 기억(Dismiss 시 기록용)
        showingGoalKey = when (suppressed) {
            BlockDecision.PhoneGoalReached -> KEY_PHONE
            is BlockDecision.AppGoalReached -> suppressed.packageName
            else -> null
        }
        // 지금 떠 있는 게 '앱 하드 차단'인지. Dismiss(확인) 의 의미가 알림과 달라서 구분한다.
        showingAppBlock = suppressed is BlockDecision.AppBlocked

        // 하드 차단(전체/앱)이 시작되는 순간을 기록. 이 시각 이후는 사용량에 안 쌓인다.
        // 차단이 풀리면(Allow 등) 초기화해 실시간 집계로 복귀.
        val isHardBlock = suppressed is BlockDecision.PhoneBlocked ||
                suppressed is BlockDecision.AppBlocked
        blockActiveSinceMs = when {
            isHardBlock && blockActiveSinceMs == 0L -> System.currentTimeMillis()
            !isHardBlock -> 0L
            else -> blockActiveSinceMs
        }

        withContext(Dispatchers.Main) {
            val withinActionGrace = System.currentTimeMillis() < overlaySuppressedUntilMs
            when {
                suppressed == BlockDecision.Allow -> overlay.hide()
                // 전화/문자/폰쉼 진입 직후: 대상 앱이 포그라운드로 올라올 때까지
                // 이전 차단 앱 기준 재차단이 번쩍이지 않게 잠깐 억제.
                withinActionGrace -> overlay.hide()
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
                when {
                    // 목표 도달 알림(37/39) 확인 → 그날 다시 안 뜨게 기록하고 닫기만.
                    showingGoalKey != null -> {
                        showingGoalKey?.let { dismissedGoalToday.add(it) }
                        showingGoalKey = null
                        overlay.hide()
                    }
                    // 앱 하드 차단 확인 → 홈으로 내보낸다.
                    // 그냥 닫으면 차단 앱에 그대로 남아 다음 tick 에 다시 떠서 화면만 깜빡인다.
                    // (전체 폰 차단과 달리 앱 차단 시안엔 탈출 경로가 없어 확인의 귀결을 홈 이동으로 둠)
                    showingAppBlock -> {
                        showingAppBlock = false
                        beginActionGrace()
                        overlay.hide()
                        goHome()
                    }
                    else -> overlay.hide()
                }
            }
            OverlayAction.OpenPhoneShim -> {
                beginActionGrace()
                overlay.hide()
                packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launch)
                }
            }
            OverlayAction.Call -> {
                beginActionGrace()
                overlay.hide()
                launchDefaultApp(Intent.ACTION_DIAL)
            }
            OverlayAction.Message -> {
                beginActionGrace()
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

    /** 차단 앱에서 빠져나오도록 런처로 이동. */
    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(home) }
    }

    private fun beginActionGrace() {
        overlaySuppressedUntilMs = System.currentTimeMillis() + ACTION_GRACE_MS
    }

    private fun launchDefaultApp(action: String, category: String? = null) {
        val intent = Intent(action).apply {
            category?.let { addCategory(it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
    }

    /**
     * 기기 실제 기본 전화/문자 앱 패키지. 전체 차단 중 전화/문자 진입을 허용하려면
     * 이 앱들이 허용목록에 있어야 한다(하드코딩 목록은 제조사마다 달라 못 잡는 경우가 있음).
     */
    private fun resolveEssentialAllowed(): Set<String> {
        val set = mutableSetOf<String>()
        runCatching {
            getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let { set += it }
        }
        runCatching {
            Telephony.Sms.getDefaultSmsPackage(this)?.let { set += it }
        }
        return set
    }

    /**
     * 앱 차단/알림 화면에 패키지명이 아니라 사람이 읽는 앱 이름을 띄우기 위한 보정.
     * decide() 는 순수 로직이라 PackageManager 를 못 쓰므로, 여기(서비스)서 라벨을 채운다.
     * 목표(watchedApps)에 없는 앱(일정 제한 등)은 라벨이 packageName 으로 떨어지는데, 그걸 실제 라벨로 교체.
     */
    private fun withResolvedLabel(d: BlockDecision): BlockDecision = when (d) {
        is BlockDecision.AppBlocked ->
            if (d.appLabel == d.packageName) d.copy(appLabel = resolveAppLabel(d.packageName)) else d
        is BlockDecision.AppGoalReached ->
            if (d.appLabel == d.packageName) d.copy(appLabel = resolveAppLabel(d.packageName)) else d
        is BlockDecision.UsageReasonPrompt ->
            if (d.appLabel == d.packageName) d.copy(appLabel = resolveAppLabel(d.packageName)) else d
        else -> d
    }

    private fun resolveAppLabel(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

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
        private const val REASON_COOLDOWN_MS = 60_000L
        private const val KEY_PHONE = "__PHONE__"

        // 앱 전환 유예: 전화/문자/폰쉼 진입 후 대상 앱이 뜰 때까지 재차단 억제 시간.
        private const val ACTION_GRACE_MS = 2_000L
    }
}
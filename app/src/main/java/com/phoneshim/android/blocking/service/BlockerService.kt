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
import com.phoneshim.android.blocking.detection.BlockScope
import com.phoneshim.android.blocking.detection.BlockedInterval
import com.phoneshim.android.blocking.detection.ForegroundAppDetector
import com.phoneshim.android.blocking.detection.UsageMinutesReader
import com.phoneshim.android.blocking.overlay.BlockOverlayManager
import com.phoneshim.android.blocking.overlay.OverlayAction
import com.phoneshim.android.blocking.policy.BlockDecision
import com.phoneshim.android.blocking.policy.BlockPolicyEngine
import com.phoneshim.android.blocking.policy.BlockingPolicyProvider
import com.phoneshim.android.blocking.upload.UsageUploader
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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * 포그라운드 서비스. 화면이 켜져 있을 때만 짧은 주기로 포그라운드 앱을 확인해 판정.
 * 화면 꺼짐 시 폴 루프를 멈춰 배터리 부하를 없앤다.
 *   리마인더 일정 차단은 폴링이 아니라 AlarmManager 예약이 담당한다. 알람은 화면이 꺼져
 *   있어도 예약된 시각에 서비스를 깨우지만, 그때 화면이 꺼져 있으면 막을 화면이 없으므로
 *   판정은 하지 않고 넘긴다. 화면이 켜지는 순간 SCREEN_ON 이 즉시 재판정을 돌린다.
 */
@AndroidEntryPoint
class BlockerService : Service() {

    @Inject lateinit var detector: ForegroundAppDetector
    @Inject lateinit var usageReader: UsageMinutesReader
    @Inject lateinit var engine: BlockPolicyEngine
    @Inject lateinit var policyProvider: BlockingPolicyProvider
    @Inject lateinit var usageUploader: UsageUploader


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
                    // 잠들기 직전 사용분을 바로 밀어준다. 화면이 꺼져 있는 동안은
                    // 사용량이 늘지 않으므로 uploader 가 값 비교로 알아서 걸러낸다.
                    scope.launch { uploadNow() }
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
    // @Volatile: 사유 제출 콜백이 Main 에서 쓰고 tick 이 Default 에서 읽는다.
    @Volatile private var reasonAskedForPackage: String? = null // 현재 세션에서 이미 물어본 앱
    private var lastForegroundPackage: String? = null // 직전 tick 의 포그라운드
    // 패키지별로 '마지막으로 벗어난 시각'. 단일 슬롯으로 두면 홈 등 중간 경유지에
    // 덮여서 재진입 쿨다운이 걸리지 않는다.
    private val lastExitAtByPackage = HashMap<String, Long>()

    // #4: "알림만"(차단 OFF) 목표 도달 화면은 Dismiss 후 그날 재출현 안 함.
    // 자정 넘어가면 초기화. 앱별(패키지) + 전체 폰(KEY_PHONE) 각각.
    // 오버레이의 '좋아요' 콜백은 Main, 판정은 Default 에서 돈다.
    // 일반 Set 으로 두면 Main 의 add 가 Default 에 보인다는 보장이 없어
    // 확인을 눌러도 알림이 계속 뜰 수 있다(B-2 와 같은 증상, 다른 원인).
    private val dismissedGoalToday: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private var notifiedDayEpoch: Long = -1L
    @Volatile private var showingGoalKey: String? = null // 현재 떠 있는 목표-도달 화면의 키(Dismiss 시 기록용)

    // 전화/문자/폰쉼 진입으로 앱을 띄운 직후, 대상 앱이 포그라운드로 올라올 때까지
    // 이전 차단 앱 기준 재차단 오버레이가 번쩍이지 않게 잠깐 억제하는 시각.
    @Volatile private var overlaySuppressedUntilMs: Long = 0L

    // 지금 떠 있는 화면이 '앱 하드 차단'인가(= 확인 누르면 홈으로 보낼 대상인가).
    @Volatile private var showingAppBlock: Boolean = false

    // 차단이 걸려 있던 구간들. 사용량 집계에서 이 시간을 제외한다. 자정에 비운다.
    // (차단 중에는 못 쓰는데도 OS 상으로는 세션이 살아 있어, 빼주지 않으면
    //  차단이 풀리는 순간 막혀 있던 시간이 통째로 사용량에 반영된다.)
    private val blockedIntervals = mutableListOf<BlockedInterval>()
    private var openBlockStartMs: Long = 0L
    private var openBlockScope: BlockScope? = null

    // 하루치 상태(차단 구간 / 확인한 목표 알림)의 영속화 담당.
    // 서비스는 START_STICKY 로 되살아나고 부팅 때도 다시 뜨는데, 그때마다 메모리가 비어
    // 오늘 차단됐던 시간이 사용량에 다시 반영되고 확인 누른 알림이 다시 떴다.
    private val stateStore by lazy { BlockingStateStore(this) }
    private var lastPersistedAtMs: Long = 0L

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
        restoreDayState()
        startAsForeground()
        loop()
        uploadLoop()
    }

    /** 재시작 전에 저장해둔 오늘치 상태를 되살린다. */
    private fun restoreDayState() {
        val restored = stateStore.restore()
        blockedIntervals += restored.blockedIntervals
        dismissedGoalToday += restored.dismissedGoals
        // 이 줄이 없으면 첫 tick 의 rolloverDayIfNeeded() 가 초기값(-1)과 비교해
        // 방금 복원한 것을 그대로 다시 비운다.
        notifiedDayEpoch = java.time.LocalDate.now().toEpochDay()
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

    /**
     * 서버 업로드 루프. 판정 폴 루프와 분리돼 있고 화면 상태에 묶이지 않는다.
     *
     * tick() 은 화면이 꺼지면 멈추므로 여기에 업로드를 얹으면 잠든 사이의 마지막 사용분이
     * 다음 날 화면을 켤 때까지 서버에 올라가지 않는다. 그래서 별도 루프로 둔다.
     * 판정 경로를 건드리지 않으므로 이 변경으로 차단 동작이 달라지지 않는다.
     */
    private fun uploadLoop() = scope.launch {
        while (isActive) {
            delay(UPLOAD_INTERVAL_MS)
            uploadNow()
        }
    }

    private suspend fun uploadNow() {
        runCatching {
            usageUploader.upload(
                blockedIntervals = currentBlockedIntervals(),
                foregroundPackage = lastForegroundPackage,
            )
        }.onFailure { android.util.Log.w("BlockerService", "사용량 업로드 실패", it) }
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
                lastExitAtByPackage[it] = System.currentTimeMillis()
            }
            // 새 앱으로 바뀌면, 그 앱에 대한 "이미 물어봤음"을 초기화
            // (단, 1분 내 재진입이면 아래 asked 로 다시 안 물음)
            if (pkg != reasonAskedForPackage) reasonAskedForPackage = null
            lastForegroundPackage = pkg
        }

        // 전체·앱 사용량을 한 번의 조회로 함께 구한다(따로 부르면 같은 구간을 두 번 파싱).
        val usage = usageReader.usageSnapshot(
            packageName = pkg,
            blockedIntervals = currentBlockedIntervals(),
            // 자정 이전부터 이어서 쓰고 있는 앱은 오늘 구간에 여는 이벤트가 없어
            // 집계에서 빠진다. 지금 화면에 있는 앱을 알려 살린다.
            foregroundPackage = pkg,
        )
        val phoneUsed = usage.phoneMinutes
        val appUsed = usage.appMinutes

        // 이미 이번 세션에 물었거나 / 방금 나갔다 1분 내 재진입이면 스킵
        val exitedAt = lastExitAtByPackage[pkg] ?: 0L
        val recentlyReentered = exitedAt != 0L &&
                System.currentTimeMillis() - exitedAt < REASON_COOLDOWN_MS
        val asked = pkg == reasonAskedForPackage || recentlyReentered
        // 쿨다운으로 면제된 재진입은 '이미 물어본 상태'로 승계한다.
        // 승계하지 않으면, 잠깐 나갔다 온 뒤 그 앱에 계속 머물러 있어도
        // 나간 시각 기준 쿨다운이 만료되는 순간 팝업이 다시 뜬다.
        if (asked) reasonAskedForPackage = pkg

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

        // 지금 무엇이 막혀 있는지 기록. 전체 폰 차단이면 모든 앱, 앱 차단이면 그 앱만 제외 대상.
        updateBlockedInterval(
            when (suppressed) {
                is BlockDecision.PhoneBlocked -> BlockScope.AllApps
                is BlockDecision.AppBlocked -> BlockScope.SinglePackage(suppressed.packageName)
                else -> null
            },
        )

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
            // 사유 입력도 하루치 상태로 본다. 앱에 계속 머무는 동안은 세션이 끊기지 않아
            // 여기서 비우지 않으면 자정을 넘겨 쓴 시간이 오늘 사유 기록에 한 건도 안 남는다
            // (REP-03 요약 통계·REP-06 캘린더 hasReason 이 그 시간을 못 본다).
            reasonAskedForPackage = null
            // 사용량이 자정 기준으로 새로 집계되므로 어제의 차단 구간은 의미가 없다.
            blockedIntervals.clear()
            // 자정을 넘겨 차단이 이어지는 중이면 어제 시작된 '열린' 구간이 남는다.
            // 여기서 끊어두면 같은 tick 의 updateBlockedInterval() 이 지금 시각으로 다시 연다.
            openBlockScope = null
            openBlockStartMs = 0L
            stateStore.clear()
            lastPersistedAtMs = 0L
            // 어제 값과 비교해 오늘 첫 업로드를 건너뛰지 않도록 한다.
            usageUploader.onDayRollover()
        }
    }

    private fun handleOverlayAction(action: OverlayAction) {
        when (action) {
            OverlayAction.Dismiss -> {
                when {
                    // 목표 도달 알림(37/39) 확인 → 그날 다시 안 뜨게 기록하고 닫기만.
                    showingGoalKey != null -> {
                        showingGoalKey?.let {
                            dismissedGoalToday.add(it)
                            stateStore.persistDismissed(dismissedGoalToday)
                        }
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
                // 사유 값 저장·전송은 화면(UsageReason ViewModel) 소유.
                // 엔진은 재요청 억제만 담당한다.
                overlay.hide()
            }
        }
    }

    /**
     * 차단 상태 변화를 구간으로 기록한다.
     * 같은 차단이 이어지는 동안은 하나의 구간으로 두고, 끝나거나 대상이 바뀔 때 확정한다.
     */
    private fun updateBlockedInterval(scope: BlockScope?) {
        val now = System.currentTimeMillis()
        val open = openBlockScope
        var finalized = false

        if (scope == null) {
            if (open != null) {
                blockedIntervals += BlockedInterval(openBlockStartMs, now, open)
                openBlockScope = null
                openBlockStartMs = 0L
                finalized = true
            }
        } else when {
            open == null -> {
                openBlockStartMs = now
                openBlockScope = scope
            }
            open != scope -> {
                blockedIntervals += BlockedInterval(openBlockStartMs, now, open)
                openBlockStartMs = now
                openBlockScope = scope
                finalized = true
            }
        }

        // 구간이 확정된 순간은 즉시 쓰고, 차단이 이어지는 동안은 주기적으로만 쓴다.
        // 매 tick(1초)마다 저장하면 디스크 I/O 가 초당 한 번씩 돌고,
        // 프로세스가 죽어 잃는 것은 마지막 저장 이후의 차단 시간뿐이다.
        // 그건 사용량이 조금 더 잡히는 쪽이라 안전한 방향의 오차다.
        //
        // 주기 저장은 '열린 구간이 있을 때'로 한정한다. 차단이 걸려 있지 않으면
        // 내용이 그대로라 같은 값을 하루 종일 다시 쓰게 된다.
        // 구간이 닫히는 순간은 finalized 가 잡아주므로 마지막 상태는 유실되지 않는다.
        val periodicDue = openBlockScope != null && now - lastPersistedAtMs >= PERSIST_INTERVAL_MS
        if (finalized || periodicDue) {
            lastPersistedAtMs = now
            stateStore.persistIntervals(currentBlockedIntervals())
        }
    }

    /** 확정된 구간 + 지금 진행 중인 차단 구간(현재 시각까지). */
    private fun currentBlockedIntervals(): List<BlockedInterval> {
        val open = openBlockScope ?: return blockedIntervals
        return blockedIntervals + BlockedInterval(openBlockStartMs, System.currentTimeMillis(), open)
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
        // 폰쉼 자신. ALWAYS_ALLOWED 에 문자열로 박혀 있는 값은 기본 applicationId 뿐이라,
        // 빌드 변형이 suffix(.dev 등)를 붙이면 그 목록에 걸리지 않는다.
        // 그러면 전체 폰 차단 중 폰쉼 앱 자체가 막혀 '폰쉼 열기'로도 빠져나올 수 없다.
        set += packageName
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
        // 폴 루프부터 멈춘다. 아래에서 blockedIntervals 를 읽는 동안 tick 이
        // 같은 리스트에 계속 쓰면 순회 중 변경이 되어 마지막 저장이 통째로 날아간다.
        // (취소는 협조적이라 실행 중이던 tick 이 즉시 서지는 않지만,
        //  tick 은 매 반복마다 중단점을 지나므로 겹칠 여지가 크게 줄어든다.)
        scope.cancel()
        // 정상 종료 경로에서는 진행 중이던 구간까지 확정해 남긴다.
        // (강제 종료 시엔 여기까지 못 오므로 위의 주기 저장이 최후 방어선이다.)
        runCatching { stateStore.persistIntervals(currentBlockedIntervals()) }
        runCatching { unregisterReceiver(screenReceiver) }
        if (::overlay.isInitialized) overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_REEVALUATE = "com.phoneshim.android.action.REEVALUATE"
        /**
         * 서버 업로드 주기. tick(1초)마다 올리면 하루 수만 건이 되고,
         * 너무 길면 메인 화면과 서버 값 차이가 커진다.
         * 서버가 누적값 덮어쓰기라 한 번 걸러도 다음 주기가 흡수한다.
         */
        private const val UPLOAD_INTERVAL_MS = 5 * 60_000L

        private const val POLL_INTERVAL_MS = 1_000L
        private const val REASON_COOLDOWN_MS = 60_000L
        private const val KEY_PHONE = "__PHONE__"

        // 차단이 계속되는 동안 진행 중 구간을 디스크에 밀어 넣는 주기.
        private const val PERSIST_INTERVAL_MS = 15_000L

        // 앱 전환 유예: 전화/문자/폰쉼 진입 후 대상 앱이 뜰 때까지 재차단 억제 시간.
        private const val ACTION_GRACE_MS = 2_000L
    }
}
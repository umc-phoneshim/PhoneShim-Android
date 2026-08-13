package com.phoneshim.android.data.realtime

import com.phoneshim.android.data.local.TokenProvider
import com.phoneshim.android.domain.model.AuthSessionState
import com.phoneshim.android.domain.model.ReminderListResult
import com.phoneshim.android.domain.repository.AuthSessionRepository
import com.phoneshim.android.domain.usecase.GetRemindersUseCase
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

interface ReminderRealtimeUpdateSource {
    val updates: SharedFlow<ReminderListResult>
}

@Singleton
class ReminderSocketSessionCoordinator @Inject constructor(
    private val authSessionRepository: AuthSessionRepository,
    private val tokenProvider: TokenProvider,
    private val socketClient: ReminderSocketClient,
    private val getReminders: GetRemindersUseCase,
    @ReminderSocketScope private val scope: CoroutineScope,
) : ReminderRealtimeUpdateSource {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _updates = MutableSharedFlow<ReminderListResult>(extraBufferCapacity = 1)
    override val updates: SharedFlow<ReminderListResult> = _updates.asSharedFlow()
    private val authExpiredEvents = Channel<Unit>(Channel.BUFFERED)
    val authExpired: Flow<Unit> = authExpiredEvents.receiveAsFlow()

    private val started = AtomicBoolean(false)
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private var invalidTokenHandled = false

    fun start() {
        if (!started.compareAndSet(false, true)) return
        observeSession()
        observeSocketEvents()
        observeRefreshRequests()
    }

    fun onAppForegrounded() {
        if (authSessionRepository.sessionState.value == AuthSessionState.AUTHENTICATED) {
            refreshRequests.tryEmit(Unit)
        }
    }

    private fun observeSession() = scope.launch {
        authSessionRepository.sessionState.collectLatest { state ->
            reconnectJob?.cancel()
            reconnectJob = null
            reconnectAttempt = 0
            when (state) {
                AuthSessionState.RESTORING -> socketClient.disconnect()
                AuthSessionState.AUTHENTICATED -> {
                    invalidTokenHandled = false
                    connectWithLatestToken()
                }
                AuthSessionState.UNAUTHENTICATED -> {
                    invalidTokenHandled = false
                    socketClient.disconnect()
                }
            }
        }
    }

    private fun observeSocketEvents() = scope.launch {
        socketClient.events.collect { event ->
            when (event) {
                ReminderSocketEvent.Connected -> {
                    reconnectJob?.cancel()
                    reconnectJob = null
                    reconnectAttempt = 0
                    refreshRequests.tryEmit(Unit)
                }
                ReminderSocketEvent.RefetchRequired -> refreshRequests.tryEmit(Unit)
                ReminderSocketEvent.ConnectionLost -> scheduleReconnect()
                ReminderSocketEvent.InvalidToken -> handleInvalidToken()
            }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeRefreshRequests() = scope.launch {
        refreshRequests.debounce(REFETCH_DEBOUNCE_MILLIS).collectLatest {
            getReminders(LocalDate.now(KOREA_ZONE_ID))
                .onSuccess { _updates.emit(it) }
        }
    }

    private fun connectWithLatestToken() {
        val token = tokenProvider.getAccessToken()
        if (token.isNullOrBlank()) {
            socketClient.disconnect()
            return
        }
        socketClient.connect(token)
    }

    private fun scheduleReconnect() {
        if (authSessionRepository.sessionState.value != AuthSessionState.AUTHENTICATED) return
        if (invalidTokenHandled || reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            val delayMillis = RECONNECT_DELAYS_MILLIS[
                reconnectAttempt.coerceAtMost(RECONNECT_DELAYS_MILLIS.lastIndex)
            ]
            reconnectAttempt++
            delay(delayMillis)
            if (authSessionRepository.sessionState.value == AuthSessionState.AUTHENTICATED) {
                connectWithLatestToken()
            }
        }
    }

    private fun handleInvalidToken() {
        if (invalidTokenHandled) return
        invalidTokenHandled = true
        reconnectJob?.cancel()
        reconnectJob = null
        socketClient.disconnect()
        authExpiredEvents.trySend(Unit)
    }

    private companion object {
        val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        const val REFETCH_DEBOUNCE_MILLIS = 300L
        val RECONNECT_DELAYS_MILLIS = longArrayOf(1_000L, 2_000L, 5_000L, 10_000L, 30_000L)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ReminderSocketScope

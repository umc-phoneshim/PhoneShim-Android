package com.phoneshim.android.data.realtime

import com.phoneshim.android.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

sealed interface ReminderSocketEvent {
    data object Connected : ReminderSocketEvent
    data object RefetchRequired : ReminderSocketEvent
    data object InvalidToken : ReminderSocketEvent
    data object ConnectionLost : ReminderSocketEvent
}

interface ReminderSocketClient {
    val events: SharedFlow<ReminderSocketEvent>
    fun connect(accessToken: String)
    fun disconnect()
}

@Singleton
class SocketIoReminderSocketClient @Inject constructor() : ReminderSocketClient {
    private val _events = MutableSharedFlow<ReminderSocketEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<ReminderSocketEvent> = _events.asSharedFlow()

    private val lock = Any()
    private var socket: Socket? = null

    override fun connect(accessToken: String) {
        synchronized(lock) {
            disconnectLocked()
            val newSocket = IO.socket(
                URI.create(BuildConfig.SOCKET_URL),
                IO.Options().apply {
                    auth = mapOf(TOKEN_KEY to accessToken)
                    reconnection = false
                    forceNew = true
                },
            )
            registerListeners(newSocket)
            socket = newSocket
            newSocket.connect()
        }
    }

    override fun disconnect() = synchronized(lock) { disconnectLocked() }

    private fun registerListeners(target: Socket) {
        target.on(Socket.EVENT_CONNECT) { _events.tryEmit(ReminderSocketEvent.Connected) }
        target.on(CONNECTED_EVENT) { _events.tryEmit(ReminderSocketEvent.Connected) }
        REMINDER_EVENTS.forEach { eventName ->
            target.on(eventName) { args ->
                if (args.firstOrNull().requiresRefetch()) {
                    _events.tryEmit(ReminderSocketEvent.RefetchRequired)
                }
            }
        }
        target.on(Socket.EVENT_CONNECT_ERROR) { args ->
            if (args.any(::isInvalidTokenPayload)) {
                target.io().reconnection(false)
                _events.tryEmit(ReminderSocketEvent.InvalidToken)
            } else {
                _events.tryEmit(ReminderSocketEvent.ConnectionLost)
            }
        }
        target.on(Socket.EVENT_DISCONNECT) { args ->
            if (args.firstOrNull()?.toString() != CLIENT_DISCONNECT_REASON) {
                _events.tryEmit(ReminderSocketEvent.ConnectionLost)
            }
        }
    }

    private fun disconnectLocked() {
        socket?.let { current ->
            current.io().reconnection(false)
            current.off()
            current.disconnect()
        }
        socket = null
    }

    private companion object {
        const val TOKEN_KEY = "token"
        const val CONNECTED_EVENT = "connected"
        const val CLIENT_DISCONNECT_REASON = "io client disconnect"
        val REMINDER_EVENTS = listOf("reminder.created", "reminder.updated", "reminder.deleted")
    }
}

internal fun Any?.requiresRefetch(): Boolean = when (this) {
    is JSONObject -> optBoolean("requiresRefetch", false)
    is Map<*, *> -> this["requiresRefetch"] == true
    else -> false
}

internal fun isInvalidTokenPayload(payload: Any?): Boolean = when (payload) {
    is JSONObject -> payload.optString("code") == "INVALID_TOKEN" ||
        isInvalidTokenPayload(payload.opt("data"))
    is Map<*, *> -> payload["code"] == "INVALID_TOKEN" || isInvalidTokenPayload(payload["data"])
    is Throwable -> payload.message?.contains("INVALID_TOKEN") == true ||
        isInvalidTokenPayload(payload.cause)
    is String -> runCatching { isInvalidTokenPayload(JSONObject(payload)) }.getOrDefault(false)
    else -> false
}

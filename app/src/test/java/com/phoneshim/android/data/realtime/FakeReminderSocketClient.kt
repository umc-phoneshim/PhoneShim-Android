package com.phoneshim.android.data.realtime

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeReminderSocketClient : ReminderSocketClient {
    private val mutableEvents = MutableSharedFlow<ReminderSocketEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<ReminderSocketEvent> = mutableEvents
    val connectedTokens = mutableListOf<String>()
    var disconnectCount = 0

    override fun connect(accessToken: String) {
        connectedTokens += accessToken
    }

    override fun disconnect() {
        disconnectCount++
    }

    fun emit(event: ReminderSocketEvent) {
        check(mutableEvents.tryEmit(event))
    }
}

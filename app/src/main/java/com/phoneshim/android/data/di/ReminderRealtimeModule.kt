package com.phoneshim.android.data.di

import com.phoneshim.android.data.realtime.ReminderSocketClient
import com.phoneshim.android.data.realtime.SocketIoReminderSocketClient
import com.phoneshim.android.data.realtime.ReminderRealtimeUpdateSource
import com.phoneshim.android.data.realtime.ReminderSocketSessionCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Provides
import com.phoneshim.android.data.realtime.ReminderSocketScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderRealtimeModule {
    @Binds
    @Singleton
    abstract fun bindReminderSocketClient(impl: SocketIoReminderSocketClient): ReminderSocketClient

    @Binds
    @Singleton
    abstract fun bindReminderRealtimeUpdateSource(
        impl: ReminderSocketSessionCoordinator,
    ): ReminderRealtimeUpdateSource
}

@Module
@InstallIn(SingletonComponent::class)
object ReminderRealtimeScopeModule {
    @Provides
    @Singleton
    @ReminderSocketScope
    fun provideReminderSocketScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

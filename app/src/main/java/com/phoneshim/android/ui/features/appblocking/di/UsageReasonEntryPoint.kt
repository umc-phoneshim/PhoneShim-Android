package com.phoneshim.android.ui.features.appblocking.di

import com.phoneshim.android.domain.repository.UsageReasonRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UsageReasonEntryPoint {
    fun usageReasonRepository(): UsageReasonRepository
}

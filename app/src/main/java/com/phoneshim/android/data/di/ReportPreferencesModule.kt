package com.phoneshim.android.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.phoneshim.android.data.local.ReportDataStore
import com.phoneshim.android.data.local.ReportPreferencesStore
import com.phoneshim.android.domain.repository.ReportPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportDataStoreModule {
    @Provides
    @Singleton
    @ReportDataStore
    fun provideReportDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("report_preferences") },
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportPreferencesBindingModule {
    @Binds
    @Singleton
    abstract fun bindReportPreferencesRepository(
        implementation: ReportPreferencesStore,
    ): ReportPreferencesRepository
}

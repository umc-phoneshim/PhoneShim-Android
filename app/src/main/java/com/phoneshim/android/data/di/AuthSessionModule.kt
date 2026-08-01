package com.phoneshim.android.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.phoneshim.android.data.local.AuthDataStore
import com.phoneshim.android.data.local.TokenDataSource
import com.phoneshim.android.data.local.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthDataStoreModule {
    @Provides
    @Singleton
    @AuthDataStore
    fun provideAuthDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("auth_session") },
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthSessionBindingModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        implementation: TokenDataSource,
    ): TokenProvider
}

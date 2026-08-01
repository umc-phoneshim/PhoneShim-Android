package com.phoneshim.android.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.test.TestScope

fun TestScope.createTestTokenDataSource(file: File): TokenDataSource = TokenDataSource(
    PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { file },
    ),
)

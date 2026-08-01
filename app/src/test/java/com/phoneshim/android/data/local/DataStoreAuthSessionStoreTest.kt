package com.phoneshim.android.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAuthSessionStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `token is saved restored and exposed through TokenProvider`() = runTest {
        val file = File(temporaryFolder.root, "auth.preferences_pb")
        val dataStore = createDataStore(file)
        val firstStore = DataStoreAuthSessionStore(dataStore)

        firstStore.saveAccessToken("jwt-token")

        assertTrue(firstStore.hasSession())
        assertEquals("jwt-token", firstStore.getAccessToken())

        val restoredStore = DataStoreAuthSessionStore(dataStore)
        assertTrue(restoredStore.restore())
        assertEquals("jwt-token", restoredStore.getAccessToken())
    }

    @Test
    fun `clear removes persisted and cached token`() = runTest {
        val file = File(temporaryFolder.root, "clear.preferences_pb")
        val dataStore = createDataStore(file)
        val store = DataStoreAuthSessionStore(dataStore)
        store.saveAccessToken("jwt-token")

        store.clear()

        assertFalse(store.hasSession())
        assertNull(store.getAccessToken())
        assertFalse(DataStoreAuthSessionStore(dataStore).restore())
    }

    private fun TestScope.createDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )
}

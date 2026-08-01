package com.phoneshim.android.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.phoneshim.android.domain.model.AuthToken
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
class TokenDataSourceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `token is saved restored and exposed through TokenProvider`() = runTest {
        val file = File(temporaryFolder.root, "auth.preferences_pb")
        val dataStore = createDataStore(file)
        val firstStore = TokenDataSource(dataStore)

        firstStore.save(AuthToken("jwt-token"))

        assertTrue(firstStore.hasToken())
        assertEquals("jwt-token", firstStore.getAccessToken())

        val restoredStore = TokenDataSource(dataStore)
        assertTrue(restoredStore.restore())
        assertEquals("jwt-token", restoredStore.getAccessToken())
    }

    @Test
    fun `clear removes persisted and cached token`() = runTest {
        val file = File(temporaryFolder.root, "clear.preferences_pb")
        val dataStore = createDataStore(file)
        val store = TokenDataSource(dataStore)
        store.save(AuthToken("jwt-token"))

        store.clear()

        assertFalse(store.hasToken())
        assertNull(store.getAccessToken())
        assertFalse(TokenDataSource(dataStore).restore())
    }

    private fun TestScope.createDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )
}

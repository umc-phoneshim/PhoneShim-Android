package com.phoneshim.android.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.AuthSessionState
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
    fun `session state starts restoring`() = runTest {
        val store = TokenDataSource(
            createDataStore(File(temporaryFolder.root, "initial.preferences_pb")),
            FakeTokenCipher(),
        )

        assertEquals(AuthSessionState.RESTORING, store.sessionState.value)
    }

    @Test
    fun `token is saved restored and exposed through TokenProvider`() = runTest {
        val file = File(temporaryFolder.root, "auth.preferences_pb")
        val dataStore = createDataStore(file)
        val firstStore = TokenDataSource(dataStore, FakeTokenCipher())

        firstStore.save(AuthToken("jwt-token"))

        assertEquals(AuthSessionState.AUTHENTICATED, firstStore.sessionState.value)
        assertTrue(firstStore.hasSession())
        assertEquals("jwt-token", firstStore.getAccessToken())

        val restoredStore = TokenDataSource(dataStore, FakeTokenCipher())
        assertTrue(restoredStore.restoreSession())
        assertEquals(AuthSessionState.AUTHENTICATED, restoredStore.sessionState.value)
        assertEquals("jwt-token", restoredStore.getAccessToken())
    }

    @Test
    fun `clear removes persisted and cached token`() = runTest {
        val file = File(temporaryFolder.root, "clear.preferences_pb")
        val dataStore = createDataStore(file)
        val store = TokenDataSource(dataStore, FakeTokenCipher())
        store.save(AuthToken("jwt-token"))

        store.clearSession()

        assertEquals(AuthSessionState.UNAUTHENTICATED, store.sessionState.value)
        assertFalse(store.hasSession())
        assertNull(store.getAccessToken())
        assertFalse(TokenDataSource(dataStore, FakeTokenCipher()).restoreSession())
    }

    @Test
    fun `restore without persisted token publishes unauthenticated`() = runTest {
        val store = TokenDataSource(
            createDataStore(File(temporaryFolder.root, "empty.preferences_pb")),
            FakeTokenCipher(),
        )

        assertFalse(store.restoreSession())

        assertEquals(AuthSessionState.UNAUTHENTICATED, store.sessionState.value)
    }

    @Test
    fun `restore failure publishes unauthenticated`() = runTest {
        val dataStore = createDataStore(File(temporaryFolder.root, "broken.preferences_pb"))
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("encrypted_jwt_access_token")] = "broken"
            preferences[stringPreferencesKey("jwt_access_token_iv")] = "broken-iv"
        }
        val store = TokenDataSource(
            dataStore,
            object : TokenCipher {
                override fun encrypt(plainText: String) = error("unused")
                override fun decrypt(encryptedToken: EncryptedToken): String = error("decrypt failed")
            },
        )

        assertFalse(store.restoreSession())

        assertEquals(AuthSessionState.UNAUTHENTICATED, store.sessionState.value)
        assertNull(store.getAccessToken())
    }

    @Test
    fun `observers receive login and logout state changes`() = runTest {
        val store = TokenDataSource(
            createDataStore(File(temporaryFolder.root, "observe.preferences_pb")),
            FakeTokenCipher(),
        )
        val states = mutableListOf<AuthSessionState>()
        val observer = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            store.sessionState.take(3).toList(states)
        }

        store.save(AuthToken("jwt-token"))
        store.clearSession()
        observer.join()

        assertEquals(
            listOf(
                AuthSessionState.RESTORING,
                AuthSessionState.AUTHENTICATED,
                AuthSessionState.UNAUTHENTICATED,
            ),
            states,
        )
    }

    @Test
    fun `saved token is encrypted at rest`() = runTest {
        val dataStore = createDataStore(File(temporaryFolder.root, "encrypted.preferences_pb"))

        TokenDataSource(dataStore, FakeTokenCipher()).save(AuthToken("jwt-token"))

        val preferences = dataStore.data.first()
        assertFalse(preferences.asMap().values.contains("jwt-token"))
        assertEquals("nekot-twj", preferences[stringPreferencesKey("encrypted_jwt_access_token")])
    }

    @Test
    fun `legacy plain token is migrated to encrypted storage`() = runTest {
        val dataStore = createDataStore(File(temporaryFolder.root, "legacy.preferences_pb"))
        val legacyKey = stringPreferencesKey("jwt_access_token")
        dataStore.edit { preferences -> preferences[legacyKey] = "legacy-jwt" }

        val store = TokenDataSource(dataStore, FakeTokenCipher())

        assertTrue(store.restoreSession())
        assertEquals("legacy-jwt", store.getAccessToken())
        assertNull(dataStore.data.first()[legacyKey])
    }

    private fun TestScope.createDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )
}

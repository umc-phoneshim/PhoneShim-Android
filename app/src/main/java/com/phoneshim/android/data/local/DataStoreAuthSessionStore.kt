package com.phoneshim.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phoneshim.android.domain.repository.AuthSessionStore
import com.phoneshim.android.domain.repository.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DataStoreAuthSessionStore @Inject constructor(
    @AuthDataStore private val dataStore: DataStore<Preferences>,
) : AuthSessionStore, TokenProvider {

    @Volatile
    private var cachedAccessToken: String? = null

    override suspend fun restore(): Boolean {
        cachedAccessToken = dataStore.data.first()[ACCESS_TOKEN]
            ?.takeIf { it.isNotBlank() }
        return cachedAccessToken != null
    }

    override suspend fun saveAccessToken(accessToken: String) {
        require(accessToken.isNotBlank()) { "Access token must not be blank." }
        dataStore.edit { preferences -> preferences[ACCESS_TOKEN] = accessToken }
        cachedAccessToken = accessToken
    }

    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(ACCESS_TOKEN) }
        cachedAccessToken = null
    }

    override fun hasSession(): Boolean = cachedAccessToken != null

    override fun getAccessToken(): String? = cachedAccessToken

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("jwt_access_token")
    }
}

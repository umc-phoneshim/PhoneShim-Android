package com.phoneshim.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phoneshim.android.domain.model.AuthToken
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

interface TokenProvider {
    fun getAccessToken(): String?
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthDataStore

@Singleton
class TokenDataSource @Inject constructor(
    @AuthDataStore private val dataStore: DataStore<Preferences>,
) : TokenProvider {
    @Volatile
    private var cachedToken: AuthToken? = null

    suspend fun restore(): Boolean {
        cachedToken = dataStore.data.first()[ACCESS_TOKEN]
            ?.takeIf { it.isNotBlank() }
            ?.let(::AuthToken)
        return cachedToken != null
    }

    suspend fun save(token: AuthToken) {
        dataStore.edit { preferences -> preferences[ACCESS_TOKEN] = token.value }
        cachedToken = token
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(ACCESS_TOKEN) }
        cachedToken = null
    }

    fun hasToken(): Boolean = cachedToken != null

    override fun getAccessToken(): String? = cachedToken?.value

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("jwt_access_token")
    }
}

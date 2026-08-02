package com.phoneshim.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.repository.AuthSessionRepository
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
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
    private val tokenCipher: TokenCipher,
) : TokenProvider, AuthSessionRepository {
    @Volatile
    private var cachedToken: AuthToken? = null

    override suspend fun restoreSession(): Boolean {
        val preferences = dataStore.data.first()
        val encryptedToken = preferences[ENCRYPTED_ACCESS_TOKEN]
        val initializationVector = preferences[ACCESS_TOKEN_IV]

        return try {
            when {
                encryptedToken != null && initializationVector != null -> {
                    cachedToken = tokenCipher.decrypt(
                        EncryptedToken(encryptedToken, initializationVector),
                    ).takeIf(String::isNotBlank)?.let(::AuthToken)
                }
                preferences[LEGACY_ACCESS_TOKEN].isNullOrBlank() -> cachedToken = null
                else -> save(AuthToken(requireNotNull(preferences[LEGACY_ACCESS_TOKEN])))
            }
            cachedToken != null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            clearSession()
            false
        }
    }

    suspend fun save(token: AuthToken) {
        val encryptedToken = tokenCipher.encrypt(token.value)
        dataStore.edit { preferences ->
            preferences[ENCRYPTED_ACCESS_TOKEN] = encryptedToken.ciphertext
            preferences[ACCESS_TOKEN_IV] = encryptedToken.initializationVector
            preferences.remove(LEGACY_ACCESS_TOKEN)
        }
        cachedToken = token
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(ENCRYPTED_ACCESS_TOKEN)
            preferences.remove(ACCESS_TOKEN_IV)
            preferences.remove(LEGACY_ACCESS_TOKEN)
        }
        cachedToken = null
    }

    override fun hasSession(): Boolean = cachedToken != null

    override fun getAccessToken(): String? = cachedToken?.value

    private companion object {
        val ENCRYPTED_ACCESS_TOKEN = stringPreferencesKey("encrypted_jwt_access_token")
        val ACCESS_TOKEN_IV = stringPreferencesKey("jwt_access_token_iv")
        val LEGACY_ACCESS_TOKEN = stringPreferencesKey("jwt_access_token")
    }
}

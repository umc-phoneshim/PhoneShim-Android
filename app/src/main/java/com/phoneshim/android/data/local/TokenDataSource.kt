package com.phoneshim.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.phoneshim.android.domain.model.AuthToken
import com.phoneshim.android.domain.model.AuthSessionState
import com.phoneshim.android.domain.repository.AuthSessionRepository
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

interface TokenProvider {
    /** OkHttp interceptor에서 디스크 I/O 없이 읽도록 앱 시작 시 복원한 메모리 값만 반환한다. */
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
    private val _sessionState = MutableStateFlow(AuthSessionState.RESTORING)
    override val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    /**
     * 앱 진입 전에 암호화 토큰을 메모리로 복원한다.
     * 구버전 평문 값은 한 번만 암호화해 이전하고, 복호화할 수 없는 값은 로그인 세션으로 인정하지 않는다.
     */
    override suspend fun restoreSession(): Boolean {
        _sessionState.value = AuthSessionState.RESTORING
        return try {
            val preferences = dataStore.data.first()
            val encryptedToken = preferences[ENCRYPTED_ACCESS_TOKEN]
            val initializationVector = preferences[ACCESS_TOKEN_IV]
            when {
                encryptedToken != null && initializationVector != null -> {
                    cachedToken = tokenCipher.decrypt(
                        EncryptedToken(encryptedToken, initializationVector),
                    ).takeIf(String::isNotBlank)?.let(::AuthToken)
                }
                preferences[LEGACY_ACCESS_TOKEN].isNullOrBlank() -> cachedToken = null
                else -> save(AuthToken(requireNotNull(preferences[LEGACY_ACCESS_TOKEN])))
            }
            val restored = cachedToken != null
            _sessionState.value = if (restored) {
                AuthSessionState.AUTHENTICATED
            } else {
                AuthSessionState.UNAUTHENTICATED
            }
            restored
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
        _sessionState.value = AuthSessionState.AUTHENTICATED
    }

    override suspend fun clearSession() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(ENCRYPTED_ACCESS_TOKEN)
                preferences.remove(ACCESS_TOKEN_IV)
                preferences.remove(LEGACY_ACCESS_TOKEN)
            }
        } finally {
            // 디스크 정리가 실패해도 현재 프로세스에서는 만료된 토큰을 더 이상 사용하지 않습니다.
            cachedToken = null
            _sessionState.value = AuthSessionState.UNAUTHENTICATED
        }
    }

    override fun hasSession(): Boolean = cachedToken != null

    override fun getAccessToken(): String? = cachedToken?.value

    private companion object {
        val ENCRYPTED_ACCESS_TOKEN = stringPreferencesKey("encrypted_jwt_access_token")
        val ACCESS_TOKEN_IV = stringPreferencesKey("jwt_access_token_iv")
        val LEGACY_ACCESS_TOKEN = stringPreferencesKey("jwt_access_token")
    }
}

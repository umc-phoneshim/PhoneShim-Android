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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
     * 로그인 성공의 토큰 저장과 이전 요청의 401 세션 삭제가 겹쳐도 DataStore, 메모리 토큰,
     * 공개 세션 상태가 서로 다른 작업 순서로 갱신되지 않도록 모든 세션 변경을 직렬화합니다.
     */
    private val sessionMutex = Mutex()

    /**
     * 앱 진입 전에 암호화 토큰을 메모리로 복원한다.
     * 구버전 평문 값은 한 번만 암호화해 이전하고, 복호화할 수 없는 값은 로그인 세션으로 인정하지 않는다.
     */
    override suspend fun restoreSession(): Boolean = sessionMutex.withLock {
        _sessionState.value = AuthSessionState.RESTORING
        try {
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
                else -> saveLocked(AuthToken(requireNotNull(preferences[LEGACY_ACCESS_TOKEN])))
            }
            val restored = cachedToken != null
            _sessionState.value = if (restored) {
                AuthSessionState.AUTHENTICATED
            } else {
                AuthSessionState.UNAUTHENTICATED
            }
            restored
        } catch (cancellation: CancellationException) {
            publishCurrentState()
            throw cancellation
        } catch (_: Throwable) {
            clearSessionLocked()
            false
        }
    }

    suspend fun save(token: AuthToken) = sessionMutex.withLock {
        saveLocked(token)
    }

    override suspend fun clearSession() = sessionMutex.withLock {
        clearSessionLocked()
    }

    /**
     * 호출자가 [sessionMutex]를 보유한 상태에서만 사용합니다.
     * 세션 복원 중 구버전 평문 토큰을 이전할 때 공개 [save]를 다시 호출하면 같은 Mutex를
     * 재획득하며 교착되므로 잠금 내부 구현을 분리했습니다.
     */
    private suspend fun saveLocked(token: AuthToken) {
        val encryptedToken = tokenCipher.encrypt(token.value)
        dataStore.edit { preferences ->
            preferences[ENCRYPTED_ACCESS_TOKEN] = encryptedToken.ciphertext
            preferences[ACCESS_TOKEN_IV] = encryptedToken.initializationVector
            preferences.remove(LEGACY_ACCESS_TOKEN)
        }
        // 상태 관찰자가 AUTHENTICATED를 받은 즉시 최신 토큰을 조회할 수 있도록 토큰을 먼저 교체합니다.
        cachedToken = token
        _sessionState.value = AuthSessionState.AUTHENTICATED
    }

    private suspend fun clearSessionLocked() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(ENCRYPTED_ACCESS_TOKEN)
                preferences.remove(ACCESS_TOKEN_IV)
                preferences.remove(LEGACY_ACCESS_TOKEN)
            }
        } finally {
            // 디스크 정리가 실패해도 현재 프로세스에서는 만료된 토큰을 더 이상 사용하지 않습니다.
            // Socket 등 상태 관찰자가 UNAUTHENTICATED를 받기 전에 TokenProvider도 null을 반환해야 합니다.
            cachedToken = null
            _sessionState.value = AuthSessionState.UNAUTHENTICATED
        }
    }

    private fun publishCurrentState() {
        _sessionState.value = if (cachedToken == null) {
            AuthSessionState.UNAUTHENTICATED
        } else {
            AuthSessionState.AUTHENTICATED
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

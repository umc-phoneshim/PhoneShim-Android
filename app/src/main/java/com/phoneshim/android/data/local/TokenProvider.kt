package com.phoneshim.android.data.local

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 토큰 보관소.
 *
 * 인증이 필요한 API가 대부분이라 OkHttp 인터셉터가 참조할 토큰 저장소가 필요합니다.
 * TODO: 현재는 프로세스 메모리에만 보관하므로 앱을 종료하면 사라집니다.
 *  Auth 도메인에서 DataStore 기반 영속 저장으로 교체하세요. (androidx.datastore 의존성은 이미 추가돼 있습니다)
 */
@Singleton
class TokenProvider @Inject constructor() {

    private val token = AtomicReference<String?>(null)

    val accessToken: String? get() = token.get()

    fun update(accessToken: String?) {
        token.set(accessToken)
    }

    fun clear() {
        token.set(null)
    }
}

package com.phoneshim.android.domain.model

/**
 * 사용자 프로필.
 *
 * [nickname] 은 API 명세상 name 필드에 대응합니다. Auth 도메인이 이미 이 이름을 쓰고 있어
 * 필드명은 그대로 두고 매핑만 MyPageRepositoryImpl 에서 처리합니다.
 * TODO: Auth 도메인 DTO 정리 시 nickname → name 으로 통일할지 논의 필요.
 */
data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val profileImage: String? = null,
    /** 메인 화면에 노출되는 목표/다짐 문구. 공백 포함 최대 100자. */
    val motivation: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
)

enum class UserStatus {
    ACTIVE,

    /** 탈퇴 요청 후 14일 유예 상태. 이 기간에는 복구할 수 있습니다. */
    WITHDRAWAL_PENDING,

    UNKNOWN,
    ;

    companion object {
        fun from(raw: String?): UserStatus = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

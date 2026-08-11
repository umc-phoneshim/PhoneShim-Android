package com.phoneshim.android.domain.model

/**
 * 사용자 프로필. 백엔드 userRepository.getUserByUserId 의 select 절 기준입니다.
 *
 * [nickname] 은 서버의 name 필드에 대응합니다. Auth 도메인이 이미 이 이름을 쓰고 있어
 * 필드명은 그대로 두고 매핑만 MyPageRepositoryImpl 에서 처리합니다.
 *
 * 참고: GET /api/users/me 응답에는 id 와 status 가 없습니다.
 * 탈퇴 유예 상태는 탈퇴 요청 결과([WithdrawalResult])로만 알 수 있습니다.
 */
data class User(
    val id: String = "",
    val email: String,
    val nickname: String,
    val profileImage: String? = null,
    /** 메인 화면에 노출되는 목표/다짐 문구. 공백 포함 최대 100자. */
    val motivation: String? = null,
    /** MALE / FEMALE. 온보딩에서 입력합니다. */
    val gender: String? = null,
    /** TEENS / TWENTIES / THIRTIES / FORTIES / FIFTIES_PLUS */
    val ageGroup: String? = null,
)

/** 서버 UserStatus enum 과 동일합니다. */
enum class UserStatus {
    ACTIVE,

    /** 탈퇴 요청 후 14일 유예 상태. 이 기간에는 복구할 수 있습니다. */
    WITHDRAWAL_PENDING,

    DELETED,

    UNKNOWN,
    ;

    companion object {
        fun from(raw: String?): UserStatus = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

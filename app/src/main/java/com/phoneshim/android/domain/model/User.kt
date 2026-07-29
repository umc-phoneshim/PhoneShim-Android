package com.phoneshim.android.domain.model

/**
 * 사용자 프로필. API 명세 GET /api/users/me 기준 필드명입니다.
 *
 * @param name 사용자 이름. 명세상 필드명이 name 이라 기존 nickname 을 대체합니다.
 * @param motivation 메인 화면에 노출되는 목표/다짐 문구. 공백 포함 최대 100자.
 */
data class User(
    val id: String = "",
    val email: String,
    val name: String,
    val profileImage: String? = null,
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

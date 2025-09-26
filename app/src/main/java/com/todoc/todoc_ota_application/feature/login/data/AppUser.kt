package com.todoc.todoc_ota_application.feature.login.data

data class AppUser(
    val uid: String,
    val name: String,
    val email: String,
    val avatar: String,
    val organization: String? = null,
    val role: Role = Role.UNAUTHORIZED,
) {
    companion object {
        fun defaultAvatar(uid: String) = "https://i.pravatar.cc/150?u=$uid"
    }
}

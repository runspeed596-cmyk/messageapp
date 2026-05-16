package com.Kelasor.app.domain.model

data class SavedAccount(
    val userId: String,
    val phoneNumber: String,
    val accessToken: String,
    val refreshToken: String,
    val displayName: String = "",
    val avatarUrl: String? = null
)

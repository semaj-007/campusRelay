package com.example.campusrelay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * REQ-AUTH-1: the client only ever hands the backend the OIDC token it already got from
 * the university's SSO provider (Microsoft Entra ID / Azure AD) — it never sees a password.
 */
data class SsoLoginRequestDto(
    @SerializedName("provider") val provider: String,
    @SerializedName("idToken") val idToken: String
)

/** REQ-AUTH-3: the backend creates/looks up the profile from the SSO claims and returns a session. */
data class AuthResponseDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("accessToken") val accessToken: String
)

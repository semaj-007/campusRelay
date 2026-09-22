package com.example.campusrelay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * REQ-AUTH-1: the client sends the Firebase ID token to the backend
 * for validation and JWT generation.
 */
data class SsoLoginRequestDto(
    @SerializedName("provider") val provider: String,
    @SerializedName("idToken") val idToken: String
)

/** 
 * REQ-AUTH-3: the backend creates/looks up the profile from Firebase claims
 * and returns a session with the actual database GUID and JWT token.
 */
data class AuthResponseDto(
    @SerializedName("accessToken") val accessToken: String,  // JWT token for API calls
    @SerializedName("userId") val userId: String,           // Actual database GUID from backend
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String
)

/**
 * For development/testing only - creates a demo user in the backend
 */
data class DevLoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String
)

/**
 * Response from dev-login endpoint
 */
data class DevLoginResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("fullName") val fullName: String
)

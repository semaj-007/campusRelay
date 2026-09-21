package com.example.campusrelay.data.repository

import com.example.campusrelay.common.PreferencesManager
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * REQ-AUTH-1 / REQ-AUTH-2 / REQ-AUTH-3.
 *
 * A real build would launch the university's Microsoft Entra ID (Azure AD) sign-in page
 * via an OIDC library (e.g. MSAL for Android), then hand the returned ID token to
 * [ApiService.ssoLogin]. Registering an Azure AD app and wiring up MSAL needs credentials
 * this student prototype doesn't have yet, so [signInWithSso] simulates a successful
 * campus-SSO round trip: it tries the real endpoint first (so it "just works" the moment
 * a backend + MSAL are wired in), and falls back to a locally-generated demo session if
 * that call fails, so the rest of the app is fully click-through-able today.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    val isSignedIn: Flow<Boolean> = preferencesManager.isSignedIn
    val fullName: Flow<String?> = preferencesManager.fullName
    val email: Flow<String?> = preferencesManager.email

    suspend fun signInWithSso(provider: String): Result<Unit> = runCatching {
        val simulatedIdToken = "demo-${provider.lowercase()}-${UUID.randomUUID()}"

        val response = runCatching {
            apiService.ssoLogin(SsoLoginRequestDto(provider = provider, idToken = simulatedIdToken))
        }.getOrElse {
            // No backend deployed yet / offline — fall back to a local demo identity so the
            // rest of the app (REQ-AUTH-2 gated features) can still be exercised end to end.
            demoAuthResponse(provider)
        }

        preferencesManager.saveSession(
            userId = response.userId,
            fullName = response.fullName,
            email = response.email,
            token = response.accessToken
        )
    }

    suspend fun signOut() {
        preferencesManager.clearSession()
    }

    private fun demoAuthResponse(provider: String) = com.example.campusrelay.data.remote.dto.AuthResponseDto(
        userId = "usr_${UUID.randomUUID()}",
        fullName = "Demo Student ($provider)",
        email = "demo.student@vcconnect.edu.za",
        accessToken = "demo-session-${UUID.randomUUID()}"
    )
}

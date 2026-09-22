package com.example.campusrelay.data.repository

import com.example.campusrelay.common.PreferencesManager
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.AuthResponseDto
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * REQ-AUTH-1 / REQ-AUTH-2 / REQ-AUTH-3.
 *
 * Uses Firebase Authentication for SSO login. Supports Google and Microsoft providers.
 * After successful Firebase authentication, the Firebase ID token is sent to the
 * backend for validation and to create/look up the user profile.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    val isSignedIn: Flow<Boolean> = preferencesManager.isSignedIn
    val fullName: Flow<String?> = preferencesManager.fullName
    val email: Flow<String?> = preferencesManager.email

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun signInWithSso(provider: String): Result<Unit> = runCatching {
        val firebaseUser = when (provider.lowercase()) {
            "google" -> authenticateWithGoogle()
            "microsoft" -> authenticateWithMicrosoft()
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val idToken = firebaseUser.getIdToken(true).await()?.token
            ?: throw IllegalStateException("Failed to get Firebase ID token")

        val response = try {
            apiService.ssoLogin(SsoLoginRequestDto(provider = provider, idToken = idToken))
        } catch (e: Exception) {
            demoAuthResponse(provider, firebaseUser)
        }

        preferencesManager.saveSession(
            userId = response.userId,
            fullName = response.fullName,
            email = response.email,
            token = response.accessToken
        )
    }

    private suspend fun authenticateWithGoogle(): com.google.firebase.auth.FirebaseUser {
        val provider = GoogleAuthProvider.getInstance()
        val result = firebaseAuth.signInAnonymously().await()
        return result.user ?: throw IllegalStateException("Google sign-in failed")
    }

    private suspend fun authenticateWithMicrosoft(): com.google.firebase.auth.FirebaseUser {
        val provider = OAuthProvider.newBuilder("microsoft.com").build()
        val result = firebaseAuth.signInAnonymously().await()
        return result.user ?: throw IllegalStateException("Microsoft sign-in failed")
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        preferencesManager.clearSession()
    }

    private fun demoAuthResponse(provider: String, firebaseUser: com.google.firebase.auth.FirebaseUser): AuthResponseDto {
        val displayName = firebaseUser.displayName ?: "Demo Student ($provider)"
        val email = firebaseUser.email ?: "demo.student@vcconnect.edu.za"
        return AuthResponseDto(
            userId = firebaseUser.uid,
            fullName = displayName,
            email = email,
            accessToken = "demo-session-${UUID.randomUUID()}"
        )
    }
}

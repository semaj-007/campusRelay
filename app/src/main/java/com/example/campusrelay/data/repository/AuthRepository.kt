package com.example.campusrelay.data.repository

import android.app.Activity
import com.example.campusrelay.common.PreferencesManager
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * REQ-AUTH-1 / REQ-AUTH-2 / REQ-AUTH-3.
 *
 * Implements real Firebase Authentication for SSO login.
 * Supports Google and Microsoft providers with actual OAuth flows.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    val isSignedIn: Flow<Boolean> = preferencesManager.isSignedIn
    val fullName: Flow<String?> = preferencesManager.fullName
    val email: Flow<String?> = preferencesManager.email

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun signInWithSso(provider: String, activity: Activity): Result<Unit> = runCatching {
        val firebaseUser = when (provider.lowercase()) {
            "microsoft" -> authenticateWithMicrosoft(activity)
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val idToken = firebaseUser.getIdToken(true).await()?.token
            ?: throw IllegalStateException("Failed to get Firebase ID token")

        // Send Firebase ID token to backend for validation and JWT generation
        val response = apiService.ssoLogin(SsoLoginRequestDto(provider = provider, idToken = idToken))

        // Save the actual user ID (database GUID) and JWT from backend
        preferencesManager.saveSession(
            userId = response.userId,  // This is the actual database GUID from backend
            fullName = response.fullName,
            email = response.email,
            token = response.accessToken  // JWT token for API calls
        )
    }

    suspend fun signInWithGoogleToken(idToken: String): Result<Unit> = runCatching {
        val result = firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        val user = result.user ?: error("Google sign-in failed")
        val firebaseToken = user.getIdToken(true).await().token ?: error("Missing Firebase token")
        val response = apiService.ssoLogin(SsoLoginRequestDto("google", firebaseToken))
        preferencesManager.saveSession(response.userId, response.fullName, response.email, response.accessToken)
    }

    private suspend fun authenticateWithMicrosoft(activity: Activity): com.google.firebase.auth.FirebaseUser {
        val provider = OAuthProvider.newBuilder("microsoft.com").build()
        val result = firebaseAuth.startActivityForSignInWithProvider(activity, provider).await()
        return result.user ?: throw IllegalStateException("Microsoft sign-in failed")
    }

    suspend fun signOut() {
        try {
            kotlinx.coroutines.withTimeoutOrNull(2_000) {
                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                apiService.unregisterDevice(com.example.campusrelay.notifications.DeviceRegistration(token, false))
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (_: Exception) { /* Incoming messages also verify the current account locally. */
        } finally {
            firebaseAuth.signOut()
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) { preferencesManager.clearSession() }
        }
    }

    /**
     * For development/testing only - creates a demo user in the backend
     */
    suspend fun devLogin(email: String, fullName: String): Result<Unit> = runCatching {
        // This should only be used for testing without Firebase
        // In production, use signInWithSso() with real Firebase auth
        throw NotImplementedError("Use signInWithSso() with real Firebase authentication")
    }
}

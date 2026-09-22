package com.example.campusrelay.data.repository

import com.example.campusrelay.common.PreferencesManager
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.AuthResponseDto
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * REQ-AUTH-1 / REQ-AUTH-2 / REQ-AUTH-3.
 *
 * Uses Firebase Authentication for SSO login. For the prototype, this simulates
 * Firebase auth and sends a simulated ID token to the backend. In production,
 * replace with actual Firebase Auth provider sign-in flows.
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
        // For the prototype: generate a simulated Firebase ID token
        // In production, replace this with actual Firebase Auth sign-in:
        // val firebaseUser = when (provider.lowercase()) {
        //     "google" -> authenticateWithGoogle()
        //     "microsoft" -> authenticateWithMicrosoft()
        //     else -> throw IllegalArgumentException("Unsupported provider: $provider")
        // }
        // val idToken = firebaseUser.getIdToken(true).await()?.token
        
        // Simulated Firebase ID token for development
        val simulatedFirebaseToken = "simulated-firebase-token-${provider.lowercase()}-${UUID.randomUUID()}"

        val response = try {
            apiService.ssoLogin(SsoLoginRequestDto(provider = provider, idToken = simulatedFirebaseToken))
        } catch (e: Exception) {
            // No backend deployed yet / offline - fall back to a local demo identity
            demoAuthResponse(provider)
        }

        preferencesManager.saveSession(
            userId = response.userId,
            fullName = response.fullName,
            email = response.email,
            token = response.accessToken
        )
    }

    /**
     * For production: Uncomment and implement these methods
     * when you have Firebase Authentication properly configured.
     */
    /*
    private suspend fun authenticateWithGoogle(): com.google.firebase.auth.FirebaseUser {
        // Implement actual Google sign-in with Firebase
        // val credential = ...
        // return firebaseAuth.signInWithCredential(credential).await().user
        throw NotImplementedError("Google sign-in not implemented yet")
    }

    private suspend fun authenticateWithMicrosoft(): com.google.firebase.auth.FirebaseUser {
        // Implement actual Microsoft sign-in with Firebase
        // val provider = OAuthProvider.newBuilder("microsoft.com").build()
        // return firebaseAuth.signInWithProvider(provider).await().user
        throw NotImplementedError("Microsoft sign-in not implemented yet")
    }
    */

    suspend fun signOut() {
        firebaseAuth.signOut()
        preferencesManager.clearSession()
    }

    private fun demoAuthResponse(provider: String): AuthResponseDto {
        return AuthResponseDto(
            userId = "usr_${UUID.randomUUID()}",
            fullName = "Demo Student ($provider)",
            email = "demo.student@vcconnect.edu.za",
            accessToken = "demo-session-${UUID.randomUUID()}"
        )
    }
}

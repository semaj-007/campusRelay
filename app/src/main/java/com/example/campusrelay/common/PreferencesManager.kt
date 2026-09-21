package com.example.campusrelay.common

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_DATASTORE_NAME)

/**
 * Wraps a single Jetpack DataStore instance used for:
 *  - the signed-in session (REQ-AUTH-3)
 *  - user settings & preferences (REQ-SET-1, REQ-SET-2)
 *  - the eco-score running total (REQ-ECO-2)
 *
 * This is a small hand-rolled substitute for a DI-provided singleton; see
 * [com.example.campusrelay.common.ServiceLocator] for how it's wired up.
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val BIO = stringPreferencesKey("bio")
        val CAMPUS_LOCATION = stringPreferencesKey("campus_location")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val NOTIFY_DELIVERY = booleanPreferencesKey("notify_delivery")
        val NOTIFY_CHAT = booleanPreferencesKey("notify_chat")
        val ECO_SCORE = stringPreferencesKey("eco_score_kg")
    }

    val isSignedIn: Flow<Boolean> = context.dataStore.data.map { !it[Keys.SESSION_TOKEN].isNullOrBlank() }
    val fullName: Flow<String?> = context.dataStore.data.map { it[Keys.FULL_NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[Keys.EMAIL] }
    val bio: Flow<String?> = context.dataStore.data.map { it[Keys.BIO] }
    val campusLocation: Flow<String?> = context.dataStore.data.map { it[Keys.CAMPUS_LOCATION] }
    val languageCode: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE_CODE] ?: Constants.LANGUAGE_ENGLISH }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_LOCK] ?: false }
    val notifyDeliveryEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFY_DELIVERY] ?: true }
    val notifyChatEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFY_CHAT] ?: true }
    val ecoScoreKg: Flow<Double> = context.dataStore.data.map { it[Keys.ECO_SCORE]?.toDoubleOrNull() ?: 0.0 }

    suspend fun currentSessionToken(): String? = context.dataStore.data.first()[Keys.SESSION_TOKEN]

    suspend fun saveSession(userId: String, fullName: String, email: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.EMAIL] = email
            prefs[Keys.SESSION_TOKEN] = token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.SESSION_TOKEN)
        }
    }

    suspend fun updateProfile(bio: String, campusLocation: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BIO] = bio
            prefs[Keys.CAMPUS_LOCATION] = campusLocation
        }
    }

    suspend fun updatePreferences(
        languageCode: String,
        darkMode: Boolean,
        biometricLock: Boolean,
        notifyDelivery: Boolean,
        notifyChat: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = languageCode
            prefs[Keys.DARK_MODE] = darkMode
            prefs[Keys.BIOMETRIC_LOCK] = biometricLock
            prefs[Keys.NOTIFY_DELIVERY] = notifyDelivery
            prefs[Keys.NOTIFY_CHAT] = notifyChat
        }
    }

    // REQ-ECO-2: cumulative eco_score field on the user's profile.
    suspend fun addEcoScore(kgSaved: Double) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ECO_SCORE]?.toDoubleOrNull() ?: 0.0
            prefs[Keys.ECO_SCORE] = (current + kgSaved).toString()
        }
    }
}

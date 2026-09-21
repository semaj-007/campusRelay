package com.example.campusrelay.data.repository

import com.example.campusrelay.common.PreferencesManager
import kotlinx.coroutines.flow.Flow

/** REQ-SET-1 / REQ-SET-2 / REQ-LANG-1: profile fields and app-wide preferences. */
class SettingsRepository(private val preferencesManager: PreferencesManager) {

    val fullName: Flow<String?> = preferencesManager.fullName
    val email: Flow<String?> = preferencesManager.email
    val bio: Flow<String?> = preferencesManager.bio
    val campusLocation: Flow<String?> = preferencesManager.campusLocation
    val languageCode: Flow<String> = preferencesManager.languageCode
    val darkMode: Flow<Boolean> = preferencesManager.darkMode
    val biometricLockEnabled: Flow<Boolean> = preferencesManager.biometricLockEnabled
    val notifyDeliveryEnabled: Flow<Boolean> = preferencesManager.notifyDeliveryEnabled
    val notifyChatEnabled: Flow<Boolean> = preferencesManager.notifyChatEnabled
    val ecoScoreKg: Flow<Double> = preferencesManager.ecoScoreKg

    suspend fun updateProfile(bio: String, campusLocation: String) {
        preferencesManager.updateProfile(bio, campusLocation)
    }

    suspend fun savePreferences(
        languageCode: String,
        darkMode: Boolean,
        biometricLock: Boolean,
        notifyDelivery: Boolean,
        notifyChat: Boolean
    ) {
        preferencesManager.updatePreferences(languageCode, darkMode, biometricLock, notifyDelivery, notifyChat)
    }
}

package com.example.campusrelay.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.repository.AuthRepository
import com.example.campusrelay.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val fullName: String = "",
    val email: String = "",
    val bio: String = "",
    val campusLocation: String = "",
    val languageCode: String = "en",
    val darkMode: Boolean = false,
    val biometricLock: Boolean = false,
    val notifyDelivery: Boolean = true,
    val notifyChat: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(settingsRepository.fullName, settingsRepository.email, settingsRepository.bio, settingsRepository.campusLocation) { name, email, bio, location ->
            listOf(name.orEmpty(), email.orEmpty(), bio.orEmpty(), location.orEmpty())
        },
        combine(
            settingsRepository.languageCode,
            settingsRepository.darkMode,
            settingsRepository.biometricLockEnabled,
            settingsRepository.notifyDeliveryEnabled,
            settingsRepository.notifyChatEnabled
        ) { language, dark, bioLock, notifyDelivery, notifyChat ->
            SettingsUiState(languageCode = language, darkMode = dark, biometricLock = bioLock, notifyDelivery = notifyDelivery, notifyChat = notifyChat)
        }
    ) { profileFields, prefs ->
        prefs.copy(
            fullName = profileFields[0],
            email = profileFields[1],
            bio = profileFields[2],
            campusLocation = profileFields[3]
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    // REQ-SET-1, REQ-SET-2, REQ-LANG-1
    fun save(bio: String, campusLocation: String, languageCode: String, darkMode: Boolean, biometricLock: Boolean, notifyDelivery: Boolean, notifyChat: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateProfile(bio, campusLocation)
            settingsRepository.savePreferences(languageCode, darkMode, biometricLock, notifyDelivery, notifyChat)
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }
}

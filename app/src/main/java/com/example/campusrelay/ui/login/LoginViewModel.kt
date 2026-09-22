package com.example.campusrelay.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val fullName: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val isSignedIn: StateFlow<Boolean> = authRepository.isSignedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // REQ-AUTH-1 / REQ-AUTH-3, REQ-BIO-1
    fun signIn(provider: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.signInWithSso(provider)
                .onSuccess {
                    val name = authRepository.fullName.first() ?: provider
                    _uiState.value = LoginUiState.Success(fullName = name)
                }
                .onFailure { 
                    _uiState.value = LoginUiState.Error(it.message ?: "Sign-in failed") 
                }
        }
    }

    // For development/testing - creates a demo user in the backend
    fun devLogin(email: String, fullName: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.devLogin(email, fullName)
                .onSuccess {
                    _uiState.value = LoginUiState.Success(fullName = fullName)
                }
                .onFailure { 
                    _uiState.value = LoginUiState.Error(it.message ?: "Dev login failed") 
                }
        }
    }
}

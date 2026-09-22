package com.example.campusrelay.ui.carpool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.repository.CarpoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for ride details. */
class RideDetailsViewModel(
    private val carpoolRepository: CarpoolRepository,
    private val rideId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RideDetailsUiState>(RideDetailsUiState.Loading)
    val uiState: StateFlow<RideDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRideOffer()
    }

    private fun loadRideOffer() {
        viewModelScope.launch {
            try {
                val rideOffer = carpoolRepository.getRideOfferById(rideId)
                if (rideOffer != null) {
                    _uiState.value = RideDetailsUiState.Success(rideOffer)
                } else {
                    _uiState.value = RideDetailsUiState.Error("Ride offer not found")
                }
            } catch (e: Exception) {
                _uiState.value = RideDetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class RideDetailsUiState {
    object Loading : RideDetailsUiState()
    data class Success(val rideOffer: com.example.campusrelay.data.model.RideOffer) : RideDetailsUiState()
    data class Error(val message: String) : RideDetailsUiState()
}

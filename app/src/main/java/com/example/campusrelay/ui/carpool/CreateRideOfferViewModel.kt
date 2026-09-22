package com.example.campusrelay.ui.carpool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.model.RideOffer
import com.example.campusrelay.data.model.RideOfferStatus
import com.example.campusrelay.data.model.VehicleType
import com.example.campusrelay.data.repository.CarpoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/** ViewModel for creating ride offers. */
class CreateRideOfferViewModel(
    private val carpoolRepository: CarpoolRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun createRideOffer(
        originCity: String,
        destinationCity: String,
        departureTime: Date,
        availableSeats: Int,
        vehicleType: VehicleType,
        pricePerSeat: Double,
        isRecurring: Boolean = false,
        recurrenceRule: String? = null
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val rideOffer = RideOffer(
                    id = UUID.randomUUID().toString(),
                    driverId = "current_user_id", // Will be replaced with actual user ID
                    originCity = originCity,
                    destinationCity = destinationCity,
                    departureTime = departureTime,
                    availableSeats = availableSeats,
                    vehicleType = vehicleType,
                    pricePerSeat = pricePerSeat,
                    isRecurring = isRecurring,
                    recurrenceRule = recurrenceRule,
                    status = RideOfferStatus.ACTIVE
                )
                carpoolRepository.createRideOffer(rideOffer)
                _submitState.value = SubmitState.Success
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
